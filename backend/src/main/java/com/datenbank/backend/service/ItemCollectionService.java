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
 *  - Order-Management (geordnet / ungeordnet) und Reordering
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


    // ===========================================================
    // CRUD-Operationen
    // ===========================================================

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


    // ===========================================================
    // SubItems
    // ===========================================================

    /**
     * Gibt alle SubItems einer Kollektion zurück, sortiert nach Position.
     * Wirft 404 wenn Kollektion nicht existiert.
     */
    @Transactional(readOnly = true)
    public List<CollectionSubItemDto> getSubItemsForCollection(
            Integer collectionId) {

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


    // ===========================================================
    // Order-Management (TICKET A)
    // ===========================================================

    /**
     * Schaltet eine Kollektion zwischen geordnet und ungeordnet um.
     *
     * Bei newOrder = true (geordnet):
     *  - Falls Kollektion bisher ungeordnet war: Backend vergibt
     *    automatisch Positionen 1, 2, 3... nach Erstellungszeit der SubItems
     *  - Falls Kollektion bereits geordnet war: Positionen bleiben unverändert
     *
     * Bei newOrder = false (ungeordnet):
     *  - Positionen bleiben in der DB erhalten
     *  - Frontend blendet Positionen nur aus
     *
     * @param collectionId ID der Kollektion
     * @param newOrder neuer Order-Zustand
     * @return aktualisierte Kollektion als ResponseDTO
     */
    @Transactional
    public ItemCollectionResponseDto toggleOrder(
            Integer collectionId, Boolean newOrder) {

        if (newOrder == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "order darf nicht null sein");
        }

        ItemCollection collection = findCollectionOrThrow(collectionId);

        Boolean oldOrder = collection.getCollectionOrder();

        // Order-Flag setzen
        collection.setCollectionOrder(newOrder);

        // Falls Wechsel von ungeordnet auf geordnet:
        // automatisch Positionen 1, 2, 3... vergeben
        if (Boolean.TRUE.equals(newOrder) && !Boolean.TRUE.equals(oldOrder)) {
            List<ItemCollectionSubItem> subItems = collection.getSubItems();
            if (subItems != null) {
                int position = 1;
                for (ItemCollectionSubItem sub : subItems) {
                    sub.setPosition(position);
                    position++;
                }
            }
        }

        ItemCollection saved = collectionRepository.save(collection);
        return convertToResponseDto(saved);
    }

    /**
     * Aktualisiert die Position eines SubItems in einer geordneten Kollektion.
     *
     * Reordering-Logik:
     *  - SubItem auf die Zielposition setzen
     *  - Andere SubItems entsprechend nach oben oder unten verschieben
     *
     * Beispiel: Item auf Position 5 wird auf Position 2 verschoben.
     *   Vorher:  [1, 2, 3, 4, 5, 6, 7]
     *   Nachher: [1, 5, 2, 3, 4, 6, 7]  (alle anderen werden +1 verschoben)
     *
     * @param collectionId ID der Kollektion
     * @param itemId ID des Items
     * @param newPosition neue Zielposition (1-basiert)
     */
    @Transactional
    public void updateSubItemPosition(
            Integer collectionId, Integer itemId, Integer newPosition) {

        if (newPosition == null || newPosition < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Position muss >= 1 sein");
        }

        ItemCollection collection = findCollectionOrThrow(collectionId);

        // Prüfen: Kollektion muss geordnet sein
        if (!Boolean.TRUE.equals(collection.getCollectionOrder())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Position kann nur in geordneten Kollektionen geändert werden");
        }

        List<ItemCollectionSubItem> subItems = collection.getSubItems();
        if (subItems == null || subItems.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Kollektion hat keine SubItems");
        }

        // Das zu verschiebende SubItem finden
        ItemCollectionSubItem target = subItems.stream()
            .filter(sub -> sub.getSubItem().getItemId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "SubItem nicht in Kollektion gefunden: " + itemId));

        Integer oldPosition = target.getPosition();

        // Falls Position unverändert: nichts zu tun
        if (oldPosition.equals(newPosition)) {
            return;
        }

        // Sicherstellen, dass newPosition nicht größer als die Anzahl SubItems
        int maxPosition = subItems.size();
        if (newPosition > maxPosition) {
            newPosition = maxPosition;
        }

        // Reordering-Logik:
        // - Item nach OBEN verschieben (oldPosition > newPosition):
        //   alle Items zwischen newPosition und oldPosition-1 nach UNTEN (+1)
        // - Item nach UNTEN verschieben (oldPosition < newPosition):
        //   alle Items zwischen oldPosition+1 und newPosition nach OBEN (-1)
        for (ItemCollectionSubItem sub : subItems) {
            if (sub == target) continue;

            int pos = sub.getPosition();

            if (oldPosition > newPosition) {
                // Item geht nach oben
                if (pos >= newPosition && pos < oldPosition) {
                    sub.setPosition(pos + 1);
                }
            } else {
                // Item geht nach unten
                if (pos > oldPosition && pos <= newPosition) {
                    sub.setPosition(pos - 1);
                }
            }
        }

        // Zielposition für das verschobene Item setzen
        target.setPosition(newPosition);

        // Da @Transactional aktiv ist, werden Änderungen automatisch
        // beim Methodenende persistiert
    }


    // ===========================================================
    // Hilfsmethoden
    // ===========================================================

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

        // Order-Flag setzen (jetzt Boolean!)
        collection.setCollectionOrder(
            dto.getOrder() != null ? dto.getOrder() : false);

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

        // Anzahl SubItems
        dto.setSubItemCount(
            collection.getSubItems() != null
                ? collection.getSubItems().size()
                : 0
        );

        return dto;
    }
}