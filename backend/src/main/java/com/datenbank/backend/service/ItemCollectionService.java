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
import java.util.stream.Collectors;

/**
 * Service-Schicht für die Geschäftslogik der ItemCollection-Entität.
 *
 * Aufgaben:
 *  - Umwandlung zwischen DTOs (Frontend) und Entities (Datenbank)
 *  - Verwaltung von Sub-Items mit Position
 *  - CRUD-Operationen mit korrekter Fehlerbehandlung
 */
@Service
public class ItemCollectionService {

    private final ItemCollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final ItemCollectionSubItemRepository subItemRepository;

    /**
     * Constructor-Injection: Spring übergibt automatisch die Repositories.
     */
    public ItemCollectionService(
            ItemCollectionRepository collectionRepository,
            ItemRepository itemRepository,
            ItemCollectionSubItemRepository subItemRepository) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.subItemRepository = subItemRepository;
    }


    // CRUD-Operationen


    /**
     * Liefert alle Kollektionen als Liste von ResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<ItemCollectionResponseDto> getAll() {
        return collectionRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Liefert alle Root-Kollektionen (ohne Eltern-Item).
     */
    @Transactional(readOnly = true)
    public List<ItemCollectionResponseDto> getRootCollections() {
        return collectionRepository.findByParentItemIsNull().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Liefert eine einzelne Kollektion als ResponseDTO.
     * Wirft 404, falls nicht gefunden.
     */
    @Transactional(readOnly = true)
    public ItemCollectionResponseDto getById(Integer id) {
        ItemCollection collection = findCollectionOrThrow(id);
        return convertToResponseDto(collection);
    }

    /**
     * Erstellt eine neue Kollektion aus einem CreateDTO.
     */
    @Transactional
    public ItemCollectionResponseDto create(ItemCollectionCreateDto dto) {
        ItemCollection collection = new ItemCollection();
        applyDtoToEntity(dto, collection);
        ItemCollection saved = collectionRepository.save(collection);
        return convertToResponseDto(saved);
    }

    /**
     * Aktualisiert eine existierende Kollektion.
     * Wirft 404, falls die Kollektion nicht existiert.
     */
    @Transactional
    public ItemCollectionResponseDto update(
            Integer id, ItemCollectionCreateDto dto) {
        ItemCollection collection = findCollectionOrThrow(id);
        applyDtoToEntity(dto, collection);
        ItemCollection saved = collectionRepository.save(collection);
        return convertToResponseDto(saved);
    }

    /**
     * Löscht eine Kollektion samt Sub-Items (CASCADE).
     * Wirft 404, falls nicht gefunden.
     */
    @Transactional
    public void delete(Integer id) {
        if (!collectionRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Collection nicht gefunden");
        }
        collectionRepository.deleteById(id);
    }


    // Hilfsmethoden


    /**
     * Gibt alle SubItems einer Kollektion zurück, sortiert nach Position.
     * Wirft 404 wenn Kollektion nicht existiert.
     */
    @Transactional(readOnly = true)
    public List<CollectionSubItemDto> getSubItemsForCollection(
            Integer collectionId) {

        // Prüfen ob Collection existiert
        if (!collectionRepository.existsById(collectionId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Collection nicht gefunden");
        }

        return subItemRepository
            .findByCollection_ItemCollectionIdOrderByPositionAsc(collectionId)
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


    /**
     * Holt eine Kollektion aus der DB oder wirft 404.
     */
    private ItemCollection findCollectionOrThrow(Integer id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Collection nicht gefunden"));
    }


    /**
     * Wendet die Daten eines CreateDTO auf eine ItemCollection-Entity an.
     * Wird sowohl bei Create als auch bei Update verwendet.
     */
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
        collection.setCollectionOrder(dto.getCollectionOrder());

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

    /**
     * Wandelt eine ItemCollection-Entity in ein ResponseDTO um.
     */
    private ItemCollectionResponseDto convertToResponseDto(
            ItemCollection collection) {

        ItemCollectionResponseDto dto = new ItemCollectionResponseDto();

        dto.setItemCollectionId(collection.getItemCollectionId());
        dto.setCollectionOrder(collection.getCollectionOrder());
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

            // Anzahl SubItems hinzufügen
        dto.setSubItemCount(
            collection.getSubItems() != null 
                ? collection.getSubItems().size() 
                : 0
        );

        return dto;
    }
}