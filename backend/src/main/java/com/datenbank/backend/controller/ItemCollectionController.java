package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ItemCollectionCreateDto;
import com.datenbank.backend.dto.ItemCollectionResponseDto;
import com.datenbank.backend.service.ItemCollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}