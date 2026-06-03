package com.datenbank.backend.controller;

import com.datenbank.backend.dto.CollectionSubItemDto;
import com.datenbank.backend.dto.ItemCollectionCreateDto;
import com.datenbank.backend.dto.ItemCollectionResponseDto;
import com.datenbank.backend.dto.OrderToggleDto;
import com.datenbank.backend.dto.PositionUpdateDto;
import com.datenbank.backend.service.ItemCollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST-Controller für die ItemCollection-Entität.
 *
 * Endpoints:
 *  - CRUD-Operationen (GET, POST, PUT, DELETE)
 *  - SubItems-Verwaltung
 *  - Order-Management (Toggle und Reordering)
 */
@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "*")
public class ItemCollectionController {

    private final ItemCollectionService collectionService;

    public ItemCollectionController(ItemCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ResponseEntity<List<ItemCollectionResponseDto>> getAll() {
        return ResponseEntity.ok(collectionService.getAll());
    }

    @GetMapping("/roots")
    public ResponseEntity<List<ItemCollectionResponseDto>> getRoots() {
        return ResponseEntity.ok(collectionService.getRootCollections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemCollectionResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(collectionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ItemCollectionResponseDto> create(
            @Valid @RequestBody ItemCollectionCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCollectionResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody ItemCollectionCreateDto dto) {
        return ResponseEntity.ok(collectionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gibt alle SubItems einer Kollektion zurück, sortiert nach Position.
     * GET /api/collections/{id}/items → 200 OK oder 404
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<CollectionSubItemDto>> getSubItems(
            @PathVariable Integer id) {
        return ResponseEntity.ok(
            collectionService.getSubItemsForCollection(id));
    }

    // ===========================================================
    // Order-Management (TICKET A)
    // ===========================================================

    /**
     * Schaltet eine Kollektion zwischen geordnet und ungeordnet um.
     *
     * PUT /api/collections/{id}/order
     * Body: { "order": true }  oder  { "order": false }
     *
     * Bei true: Backend vergibt automatisch Positionen 1, 2, 3...
     * Bei false: Positionen bleiben in der DB erhalten
     *
     * @param id ID der Kollektion
     * @param dto Body mit dem neuen order-Wert
     * @return aktualisierte Kollektion
     */
    @PutMapping("/{id}/order")
    public ResponseEntity<ItemCollectionResponseDto> toggleOrder(
            @PathVariable Integer id,
            @Valid @RequestBody OrderToggleDto dto) {
        return ResponseEntity.ok(
            collectionService.toggleOrder(id, dto.getOrder()));
    }

    /**
     * Ändert die Position eines SubItems in einer geordneten Kollektion.
     *
     * PUT /api/collections/{id}/items/{itemId}/position
     * Body: { "position": 3 }
     *
     * Funktioniert nur, wenn die Kollektion geordnet ist (order = true).
     * Sonst: 400 Bad Request.
     *
     * Backend berechnet automatisch die Positionen aller anderen SubItems neu.
     *
     * @param id ID der Kollektion
     * @param itemId ID des Items
     * @param dto Body mit der neuen Position
     */
    @PutMapping("/{id}/items/{itemId}/position")
    public ResponseEntity<Void> updateSubItemPosition(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody PositionUpdateDto dto) {
        collectionService.updateSubItemPosition(id, itemId, dto.getPosition());
        return ResponseEntity.noContent().build();
    }
}