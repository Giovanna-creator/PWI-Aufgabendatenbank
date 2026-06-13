package com.datenbank.backend.service;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final ItemRepresentationTemplateRepository templateRepository;
    private final ItemCollectionRepository collectionRepository;
    private final ItemCollectionSubItemRepository subItemRepository;
    private final ItemContentsRepository itemContentsRepository;
    private final ItemContentRepository contentRepository;
    private final ItemContentTypeRepository contentTypeRepository;

    public ItemService(ItemRepository itemRepository,
                       AuthorRepository authorRepository,
                       LicenseRepository licenseRepository,
                       ItemTypeRepository itemTypeRepository,
                       ItemRepresentationTemplateRepository templateRepository,
                       ItemCollectionRepository collectionRepository,
                       ItemCollectionSubItemRepository subItemRepository,
                       ItemContentsRepository itemContentsRepository,
                       ItemContentRepository contentRepository,
                       ItemContentTypeRepository contentTypeRepository) {
        this.itemRepository = itemRepository;
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.templateRepository = templateRepository;
        this.collectionRepository = collectionRepository;
        this.subItemRepository = subItemRepository;
        this.itemContentsRepository = itemContentsRepository;
        this.contentRepository = contentRepository;
        this.contentTypeRepository = contentTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<FrontendItemDto> getRootItems() {
        List<Item> items = itemRepository.findByRootItemIsNullWithDetails();
        return items.stream()
                .map(this::toFrontendDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FrontendItemDto getItemById(Integer id) {
        Item item = findItemOrThrow(id);
        return toFrontendDto(item);
    }

    @Transactional
    public FrontendItemDto createItem(FrontendCreateItemRequest request) {
        Item item = new Item();
        applyCreateRequest(request, item);
        Item saved = itemRepository.save(item);
        return toFrontendDto(saved);
    }

    @Transactional
    public void deleteItem(Integer id) {
        Item item = findItemOrThrow(id);
        collectionRepository.findByParentItem_ItemId(id)
                .ifPresent(c -> {
                    subItemRepository.deleteAll(c.getSubItems());
                    collectionRepository.delete(c);
                });
        itemRepository.delete(item);
    }

    @Transactional
    public FrontendItemDto convertToCollection(Integer itemId) {
        Item item = findItemOrThrow(itemId);
        if (collectionRepository.existsByParentItem_ItemId(itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item ist bereits eine Kollektion");
        }
        ensureItemTypeExists("collection");
        ItemType collectionType = itemTypeRepository.findByItemTypeName("collection")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ItemType 'collection' nicht gefunden"));
        item.setItemType(collectionType);
        itemRepository.save(item);

        ItemCollection collection = new ItemCollection();
        collection.setParentItem(item);
        collection.setCollectionOrder(false);
        collectionRepository.save(collection);

        return toFrontendDto(item);
    }

    @Transactional(readOnly = true)
    public List<FrontendContentDto> getContentsForItem(Integer itemId) {
        findItemOrThrow(itemId);
        return itemContentsRepository.findByItem_ItemIdWithDetails(itemId)
                .stream()
                .map(ic -> FrontendDtoMapper.toContentDto(ic.getItemContent(), ic.getPurpose()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FrontendContentDto createContentForItem(Integer itemId, FrontendCreateContentRequest request) {
        Item item = findItemOrThrow(itemId);

        ItemContentType contentType = contentTypeRepository.findByItemContentTypeName(request.getContentType())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ContentType '" + request.getContentType() + "' nicht gefunden"));

        ItemContent content = new ItemContent();
        content.setLicense(item.getLicense());
        content.setAuthor(item.getAuthor());
        content.setItemContentType(contentType);
        content.setJsonSerializedContent(request.getJsonContent());
        if (request.getBlobContent() != null) {
            content.setBlobSerializedContent(request.getBlobContent().getBytes());
        }
        ItemContent savedContent = contentRepository.save(content);

        ItemContentsId id = new ItemContentsId(item.getItemId(), savedContent.getItemContentId());
        ItemContents itemContents = new ItemContents(id, item, savedContent, request.getPurpose());
        itemContentsRepository.save(itemContents);

        return FrontendDtoMapper.toContentDto(savedContent, request.getPurpose());
    }

    private Item findItemOrThrow(Integer id) {
        return itemRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nicht gefunden"));
    }

    private void applyCreateRequest(FrontendCreateItemRequest request, Item item) {
        Author author = authorRepository.findByDescriptor(request.getAuthor())
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setDescriptor(request.getAuthor());
                    return authorRepository.save(newAuthor);
                });
        item.setAuthor(author);

        License license = licenseRepository.findByLicense("Internal-THM")
                .orElseGet(() -> {
                    License l = new License();
                    l.setLicense("Internal-THM");
                    return licenseRepository.save(l);
                });
        item.setLicense(license);

        String typeName = request.getItemType() != null ? request.getItemType() : "exercise";
        ensureItemTypeExists(typeName);
        ItemType itemType = itemTypeRepository.findByItemTypeName(typeName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ItemType '" + typeName + "' nicht gefunden"));
        item.setItemType(itemType);

        if (request.getRootItemId() != null) {
            Integer rootId = Integer.parseInt(request.getRootItemId());
            item.setRootItem(itemRepository.findById(rootId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Root Item nicht gefunden")));
        }
    }

    private void ensureItemTypeExists(String name) {
        if (itemTypeRepository.findByItemTypeName(name).isEmpty()) {
            ItemType type = new ItemType();
            type.setItemTypeName(name);
            type.setDescription(name.equals("exercise") ? "Einzelne Aufgabe" : "Sammlung von Aufgaben");
            itemTypeRepository.save(type);
        }
    }

    private FrontendItemDto toFrontendDto(Item item) {
        boolean isCollection = collectionRepository.existsByParentItem_ItemId(item.getItemId());
        Boolean order = null;
        List<ItemCollectionSubItem> subItems = null;
        if (isCollection) {
            ItemCollection collection = collectionRepository.findByParentItem_ItemId(item.getItemId())
                    .orElse(null);
            if (collection != null) {
                order = collection.getCollectionOrder();
                subItems = collection.getSubItems();
            }
        }
        return FrontendDtoMapper.toItemDto(item, isCollection, order, subItems);
    }
}
