package com.datenbank.backend.controller;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.service.ItemCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ItemCollectionController {

    private final ItemCollectionService collectionService;

    public ItemCollectionController(ItemCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/api/collections")
    public ResponseEntity<FrontendItemDto> create(@RequestBody FrontendCreateCollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.createCollection(request));
    }

    @PutMapping("/api/collections/{id}")
    public ResponseEntity<FrontendItemDto> updateOrder(
            @PathVariable Integer id,
            @RequestBody FrontendUpdateCollectionRequest request) {
        return ResponseEntity.ok(collectionService.updateCollectionOrder(id, request.getOrder()));
    }

    @GetMapping("/api/collections/{id}/items")
    public ResponseEntity<List<FrontendCollectionItemDto>> getSubItems(@PathVariable Integer id) {
        return ResponseEntity.ok(collectionService.getSubItemsForCollection(id));
    }

    @DeleteMapping("/api/collections/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Integer id,
            @PathVariable Integer itemId) {
        collectionService.removeItemFromCollection(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/collection/{id}/items")
    public ResponseEntity<FrontendCollectionItemDto> addItem(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        Integer itemId = Integer.parseInt(body.get("itemId"));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.addItemToCollection(id, itemId));
    }

    @PutMapping("/api/collection/{id}/items/{itemId}")
    public ResponseEntity<Void> updatePosition(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @RequestBody FrontendUpdatePositionRequest request) {
        collectionService.updatePosition(id, itemId, request.getPosition());
        return ResponseEntity.ok().build();
    }
}
