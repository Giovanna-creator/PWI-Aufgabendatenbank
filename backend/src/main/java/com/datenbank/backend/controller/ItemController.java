package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ItemCreateDto;
import com.datenbank.backend.dto.ItemResponseDto;
import com.datenbank.backend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für die Item-Entität.
 */
@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // GET /api/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    // POST /api/items
    @PostMapping
    public ResponseEntity<ItemResponseDto> create(@Valid @RequestBody ItemCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(dto));
    }

    // PUT /api/items/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody ItemCreateDto dto) {
        return ResponseEntity.ok(itemService.updateItem(id, dto));
    }

    // DELETE /api/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/items              → alle Items
     * GET /api/items?root=true    → nur Root-Items
     * GET /api/items?rootItemId=5 → Kinder von Item 5
     */
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAll(
            @RequestParam(required = false) Boolean root,
            @RequestParam(required = false) Integer rootItemId) {

        // root=true hat Vorrang
        if (Boolean.TRUE.equals(root)) {
            return ResponseEntity.ok(itemService.getRootItems());
        }

        // rootItemId Filter
        if (rootItemId != null) {
            return ResponseEntity.ok(itemService.getItemsByRootId(rootItemId));
        }

        // Alle Items
        return ResponseEntity.ok(itemService.getAllItems());
    }
}