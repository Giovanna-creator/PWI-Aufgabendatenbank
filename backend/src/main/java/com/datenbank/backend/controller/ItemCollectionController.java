package com.datenbank.backend.controller;

import com.datenbank.backend.dto.AddItemToCollectionDto;
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
import java.util.UUID;

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
    public ResponseEntity<ItemCollectionResponseDto> getById(@PathVariable UUID id) {
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
            @PathVariable UUID id,
            @Valid @RequestBody ItemCollectionCreateDto dto) {
        return ResponseEntity.ok(collectionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gibt alle SubItems einer Kollektion zurück, sortiert nach Position.
     * GET /api/collections/{id}/items → 200 OK oder 404
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<CollectionSubItemDto>> getSubItems(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
            collectionService.getSubItemsForCollection(id));
    }

    /**
     * Fügt ein Item zu einer Kollektion hinzu.
     * POST /api/collections/{id}/items → 201 Created
     * Body: { "itemId": "uuid" }
     */
    @PostMapping("/{id}/items")
    public ResponseEntity<CollectionSubItemDto> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddItemToCollectionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.addItemToCollection(id, dto.getItemId()));
    }

    /**
     * Schaltet die Reihenfolge einer Kollektion um.
     * PUT /api/collections/{id}/order
     * { "order": true }  → geordnet (Positionen 1, 2, 3...)
     * { "order": false } → ungeordnet (Positionen null)
     */
    @PutMapping("/{id}/order")
    public ResponseEntity<ItemCollectionResponseDto> toggleOrder(
            @PathVariable UUID id,
            @Valid @RequestBody OrderToggleDto dto) {
        return ResponseEntity.ok(
            collectionService.toggleOrder(id, dto.getOrder()));
    }

    /**
     * Aktualisiert die Position eines SubItems in einer Kollektion.
     * PUT /api/collections/{id}/items/{itemId}/position
     * { "position": 2 } → Setzt Position auf 2, Geschwister werden neu berechnet
     */
    @PutMapping("/{id}/items/{itemId}/position")
    public ResponseEntity<Void> updateSubItemPosition(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody PositionUpdateDto dto) {
        collectionService.updateSubItemPosition(id, itemId, dto.getPosition());
        return ResponseEntity.noContent().build();
    }

    /**
     * Entfernt ein Item aus einer Kollektion.
     * DELETE /api/collections/{id}/items/{itemId}
     */
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCollection(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        collectionService.removeItemFromCollection(id, itemId);
        return ResponseEntity.noContent().build();
    }

}