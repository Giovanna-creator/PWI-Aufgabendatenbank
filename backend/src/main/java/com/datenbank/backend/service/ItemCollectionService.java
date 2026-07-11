package com.datenbank.backend.service;

import com.datenbank.backend.dto.CollectionSubItemDto;
import com.datenbank.backend.dto.ItemCollectionCreateDto;
import com.datenbank.backend.dto.ItemCollectionResponseDto;
import com.datenbank.backend.dto.ItemResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemCollectionService {

    private final ItemCollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final ItemCollectionSubItemRepository subItemRepository;

    public ItemCollectionService(
            ItemCollectionRepository collectionRepository,
            ItemRepository itemRepository,
            ItemCollectionSubItemRepository subItemRepository) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.subItemRepository = subItemRepository;
    }


    @Transactional(readOnly = true)
    public List<ItemCollectionResponseDto> getAll() {
        return collectionRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemCollectionResponseDto> getRootCollections() {
        return collectionRepository.findByParentItemIsNull().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemCollectionResponseDto getById(UUID id) {
        ItemCollection collection = findCollectionOrThrow(id);
        return convertToResponseDto(collection);
    }

    @Transactional
    public ItemCollectionResponseDto create(ItemCollectionCreateDto dto) {
        ItemCollection collection = new ItemCollection();
        applyDtoToEntity(dto, collection);
        ItemCollection saved = collectionRepository.save(collection);
        return convertToResponseDto(saved);
    }

    @Transactional
    public ItemCollectionResponseDto update(
            UUID id, ItemCollectionCreateDto dto) {
        ItemCollection collection = findCollectionOrThrow(id);
        applyDtoToEntity(dto, collection);
        ItemCollection saved = collectionRepository.save(collection);
        return convertToResponseDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!collectionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Collection nicht gefunden");
        }
        collectionRepository.deleteById(id);
    }

    /** true → geordnet (Positionen 1, 2, 3…), false → ungeordnet (Positionen null) */
    @Transactional
    public ItemCollectionResponseDto toggleOrder(UUID collectionId, Boolean newOrder) {
        ItemCollection collection = findCollectionOrThrow(collectionId);
        collection.setCollectionOrder(newOrder);

        List<ItemCollectionSubItem> subItems = subItemRepository
            .findByItemCollection_ItemCollectionIdOrderByPositionAsc(collectionId);

        for (int i = 0; i < subItems.size(); i++) {
            subItems.get(i).setPosition(newOrder ? i + 1 : null);
        }

        subItemRepository.saveAll(subItems);
        return convertToResponseDto(collectionRepository.save(collection));
    }

    @Transactional
    public void updateSubItemPosition(UUID collectionId, UUID itemId, Integer newPosition) {
        List<ItemCollectionSubItem> subItems = subItemRepository
            .findByItemCollection_ItemCollectionIdOrderByPositionAsc(collectionId);

        // Altes Element finden
        ItemCollectionSubItem moved = subItems.stream()
            .filter(s -> s.getSubItem().getItemId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "SubItem nicht gefunden"));

        // Aus Liste entfernen
        subItems.remove(moved);
        moved.setPosition(newPosition);

        // An neue Position einfügen
        int targetIndex = newPosition != null ? newPosition - 1 : subItems.size();
        if (targetIndex >= 0 && targetIndex <= subItems.size()) {
            subItems.add(targetIndex, moved);
        } else {
            subItems.add(moved);
        }

        // Alle Positionen neu berechnen (1, 2, 3...)
        for (int i = 0; i < subItems.size(); i++) {
            subItems.get(i).setPosition(i + 1);
        }

        subItemRepository.saveAll(subItems);
    }

    /** Bei geordneten Collections wird die Position automatisch vergeben. */
    @Transactional
    public CollectionSubItemDto addItemToCollection(UUID collectionId, UUID itemId) {
        ItemCollection collection = findCollectionOrThrow(collectionId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item nicht gefunden"));

        Integer position = null;
        if (Boolean.TRUE.equals(collection.getCollectionOrder())) {
            List<ItemCollectionSubItem> existing = subItemRepository
                    .findByItemCollection_ItemCollectionIdOrderByPositionAsc(collectionId);
            position = existing.size() + 1;
        }

        ItemCollectionSubItem subItem = new ItemCollectionSubItem(collection, item, position);
        subItemRepository.save(subItem);

        CollectionSubItemDto dto = new CollectionSubItemDto();
        dto.setSubItemId(item.getItemId());
        dto.setPosition(position);
        return dto;
    }

    @Transactional
    public void removeItemFromCollection(UUID collectionId, UUID itemId) {
        List<ItemCollectionSubItem> subItems = subItemRepository
            .findByItemCollection_ItemCollectionIdOrderByPositionAsc(collectionId);

        ItemCollectionSubItem toRemove = subItems.stream()
            .filter(s -> s.getSubItem().getItemId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "SubItem nicht gefunden in dieser Collection"));

        subItemRepository.delete(toRemove);

        // Positionen der verbleibenden SubItems neu berechnen
        subItems.remove(toRemove);
        for (int i = 0; i < subItems.size(); i++) {
            subItems.get(i).setPosition(i + 1);
        }
        subItemRepository.saveAll(subItems);
    }


    @Transactional(readOnly = true)
    public List<CollectionSubItemDto> getSubItemsForCollection(
            UUID collectionId) {

        if (!collectionRepository.existsById(collectionId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Collection nicht gefunden");
        }

        return subItemRepository
            .findByItemCollection_ItemCollectionIdOrderByPositionAsc(collectionId)
            .stream()
            .map(sub -> {
                CollectionSubItemDto dto = new CollectionSubItemDto();
                dto.setSubItemId(sub.getSubItem().getItemId());
                dto.setPosition(sub.getPosition());

                // Optionale vollständige Item-Daten
                ItemResponseDto itemDto = new ItemResponseDto();
                itemDto.setItemId(sub.getSubItem().getItemId());
                itemDto.setAuthorId(
                    sub.getSubItem().getAuthor().getAuthorId());
                itemDto.setAuthorDescriptor(
                    sub.getSubItem().getAuthor().getDescriptor());
                dto.setItem(itemDto);

                return dto;
            })
            .collect(Collectors.toList());
    }


    private ItemCollection findCollectionOrThrow(UUID id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Collection nicht gefunden"));
    }


    private void applyDtoToEntity(
            ItemCollectionCreateDto dto, ItemCollection collection) {

        // Optionales Eltern-Item setzen
        if (dto.getParentItemId() != null) {
            Item parentItem = itemRepository
                    .findById(dto.getParentItemId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Parent Item nicht gefunden"));
            collection.setParentItem(parentItem);
        } else {
            collection.setParentItem(null);
        }

        // Reihenfolge setzen
        collection.setCollectionOrder(dto.getOrder());

        // Sub-Items setzen mit Position
        if (dto.getSubItems() != null && !dto.getSubItems().isEmpty()) {
            List<ItemCollectionSubItem> subItems = dto.getSubItems()
                    .stream()
                    .map(subDto -> {
                        Item subItem = itemRepository
                                .findById(subDto.getSubitemId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "SubItem nicht gefunden: " + subDto.getSubitemId()));

                        return new ItemCollectionSubItem(
                                collection,
                                subItem,
                                subDto.getPosition()
                        );
                    })
                    .collect(Collectors.toList());

            collection.setSubItems(subItems);
        } else {
            collection.setSubItems(new java.util.ArrayList<>());
        }
    }

    private ItemCollectionResponseDto convertToResponseDto(
            ItemCollection collection) {

        ItemCollectionResponseDto dto = new ItemCollectionResponseDto();

        dto.setItemCollectionId(collection.getItemCollectionId());
        dto.setOrder(collection.getCollectionOrder());
        dto.setCreatedAt(collection.getCreatedAt());

        // Eltern-Item ID
        if (collection.getParentItem() != null) {
            dto.setParentItemId(
                    collection.getParentItem().getItemId());
        }

        // Sub-Items mit Position
        if (collection.getSubItems() != null) {
            List<ItemCollectionResponseDto.SubItemResponseDto> subDtos =
                    collection.getSubItems().stream()
                            .map(sub -> {
                                ItemCollectionResponseDto.SubItemResponseDto subDto =
                                        new ItemCollectionResponseDto.SubItemResponseDto();
                                subDto.setSubitemId(
                                        sub.getSubItem().getItemId());
                                subDto.setPosition(sub.getPosition());
                                return subDto;
                            })
                            .collect(Collectors.toList());

            dto.setSubItems(subDtos);
        }

        dto.setSubItemCount(
            collection.getSubItems() != null 
                ? collection.getSubItems().size() 
                : 0
        );

        return dto;
    }
}