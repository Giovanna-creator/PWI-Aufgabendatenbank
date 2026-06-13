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
public class ItemCollectionService {

    private final ItemCollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final ItemCollectionSubItemRepository subItemRepository;
    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final ItemContentRepository contentRepository;
    private final ItemContentTypeRepository contentTypeRepository;
    private final ItemContentsRepository itemContentsRepository;

    public ItemCollectionService(ItemCollectionRepository collectionRepository,
                                  ItemRepository itemRepository,
                                  ItemCollectionSubItemRepository subItemRepository,
                                  AuthorRepository authorRepository,
                                  LicenseRepository licenseRepository,
                                  ItemTypeRepository itemTypeRepository,
                                  ItemContentRepository contentRepository,
                                  ItemContentTypeRepository contentTypeRepository,
                                  ItemContentsRepository itemContentsRepository) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.subItemRepository = subItemRepository;
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.contentRepository = contentRepository;
        this.contentTypeRepository = contentTypeRepository;
        this.itemContentsRepository = itemContentsRepository;
    }

    @Transactional(readOnly = true)
    public List<FrontendCollectionItemDto> getSubItemsForCollection(Integer collectionId) {
        ItemCollection collection = findCollectionOrThrow(collectionId);
        return collection.getSubItems().stream()
                .map(FrontendDtoMapper::toCollectionItemDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FrontendItemDto createCollection(FrontendCreateCollectionRequest request) {
        ensureItemTypeExists("collection");
        ItemType collectionType = itemTypeRepository.findByItemTypeName("collection")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "ItemType 'collection' nicht gefunden"));

        Author author = null;
        if (request.getAuthor() != null) {
            author = authorRepository.findByDescriptor(request.getAuthor())
                    .orElseGet(() -> {
                        Author a = new Author();
                        a.setDescriptor(request.getAuthor());
                        return authorRepository.save(a);
                    });
        }
        if (author == null) {
            author = authorRepository.findAll().stream().findFirst()
                    .orElseGet(() -> {
                        Author a = new Author();
                        a.setDescriptor("System");
                        return authorRepository.save(a);
                    });
        }

        License license = licenseRepository.findByLicense("Internal-THM")
                .orElseGet(() -> {
                    License l = new License();
                    l.setLicense("Internal-THM");
                    return licenseRepository.save(l);
                });

        Item item = new Item();
        item.setAuthor(author);
        item.setLicense(license);
        item.setItemType(collectionType);
        Item savedItem = itemRepository.save(item);

        ItemCollection collection = new ItemCollection();
        collection.setParentItem(savedItem);
        collection.setCollectionOrder(request.getOrder() != null ? request.getOrder() : false);
        collectionRepository.save(collection);

        if (request.getContents() != null) {
            for (FrontendCreateContentRequest contentReq : request.getContents()) {
                createContentForItem(savedItem, contentReq);
            }
        }

        Item refreshedItem = itemRepository.findByIdWithDetails(savedItem.getItemId())
                .orElse(savedItem);

        return FrontendDtoMapper.toItemDto(refreshedItem, true, collection.getCollectionOrder(), collection.getSubItems());
    }

    @Transactional
    public FrontendCollectionItemDto addItemToCollection(Integer collectionId, Integer itemId) {
        ItemCollection collection = findCollectionOrThrow(collectionId);
        Item subItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item nicht gefunden"));

        boolean exists = subItemRepository
                .findByCollection_ItemCollectionIdAndSubItem_ItemId(collectionId, itemId)
                .isPresent();
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item bereits in der Kollektion");
        }

        Integer nextPosition = null;
        if (Boolean.TRUE.equals(collection.getCollectionOrder())) {
            nextPosition = collection.getSubItems().stream()
                    .mapToInt(s -> s.getPosition() != null ? s.getPosition() : 0)
                    .max()
                    .orElse(0) + 1;
        }

        ItemCollectionSubItem sub = new ItemCollectionSubItem(collection, subItem, nextPosition);
        subItemRepository.save(sub);
        collection.getSubItems().add(sub);

        return FrontendDtoMapper.toCollectionItemDto(sub);
    }

    @Transactional
    public void removeItemFromCollection(Integer collectionId, Integer itemId) {
        if (!collectionRepository.existsById(collectionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection nicht gefunden");
        }
        ItemCollectionSubItem sub = subItemRepository
                .findByCollection_ItemCollectionIdAndSubItem_ItemId(collectionId, itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item nicht in der Kollektion"));
        subItemRepository.delete(sub);
    }

    @Transactional
    public void updatePosition(Integer collectionId, Integer itemId, Integer newPosition) {
        ItemCollection collection = findCollectionOrThrow(collectionId);
        ItemCollectionSubItem sub = subItemRepository
                .findByCollection_ItemCollectionIdAndSubItem_ItemId(collectionId, itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item nicht in der Kollektion"));

        if (Boolean.TRUE.equals(collection.getCollectionOrder())) {
            recalculatePositions(collection, sub, newPosition);
        } else {
            sub.setPosition(newPosition);
            subItemRepository.save(sub);
        }
    }

    @Transactional
    public FrontendItemDto updateCollectionOrder(Integer collectionId, Boolean newOrder) {
        ItemCollection collection = findCollectionOrThrow(collectionId);
        collection.setCollectionOrder(newOrder);
        collectionRepository.save(collection);

        if (Boolean.TRUE.equals(newOrder)) {
            recalculateAllPositions(collection);
        }

        Item parentItem = collection.getParentItem();
        if (parentItem == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Collection hat kein Parent-Item");
        }
        Item refreshedItem = itemRepository.findByIdWithDetails(parentItem.getItemId())
                .orElse(parentItem);
        return FrontendDtoMapper.toItemDto(refreshedItem, true, collection.getCollectionOrder(), collection.getSubItems());
    }

    private void recalculatePositions(ItemCollection collection, ItemCollectionSubItem movedSub, Integer newPosition) {
        List<ItemCollectionSubItem> siblings = collection.getSubItems().stream()
                .filter(s -> !s.equals(movedSub))
                .sorted((a, b) -> {
                    int pa = a.getPosition() != null ? a.getPosition() : Integer.MAX_VALUE;
                    int pb = b.getPosition() != null ? b.getPosition() : Integer.MAX_VALUE;
                    return Integer.compare(pa, pb);
                })
                .collect(Collectors.toList());

        movedSub.setPosition(newPosition);
        subItemRepository.save(movedSub);

        int pos = 1;
        for (ItemCollectionSubItem sibling : siblings) {
            if (pos == newPosition) pos++;
            sibling.setPosition(pos);
            subItemRepository.save(sibling);
            pos++;
        }
    }

    private void recalculateAllPositions(ItemCollection collection) {
        List<ItemCollectionSubItem> items = collection.getSubItems();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPosition(i + 1);
            subItemRepository.save(items.get(i));
        }
    }

    private ItemCollection findCollectionOrThrow(Integer id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection nicht gefunden"));
    }

    private void ensureItemTypeExists(String name) {
        if (itemTypeRepository.findByItemTypeName(name).isEmpty()) {
            ItemType type = new ItemType();
            type.setItemTypeName(name);
            type.setDescription(name.equals("exercise") ? "Einzelne Aufgabe" : "Sammlung von Aufgaben");
            itemTypeRepository.save(type);
        }
    }

    private void createContentForItem(Item item, FrontendCreateContentRequest request) {
        ItemContentType contentType = contentTypeRepository
                .findByItemContentTypeName(request.getContentType())
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
    }
}
