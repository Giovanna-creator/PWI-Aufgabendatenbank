package com.datenbank.backend.controller;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<FrontendItemDto>> getRootItems(
            @RequestParam(required = false, defaultValue = "false") Boolean root) {
        if (Boolean.TRUE.equals(root)) {
            return ResponseEntity.ok(itemService.getRootItems());
        }
        return ResponseEntity.ok(itemService.getRootItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FrontendItemDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<FrontendItemDto> create(@RequestBody FrontendCreateItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/collection")
    public ResponseEntity<FrontendItemDto> convertToCollection(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.convertToCollection(id));
    }

    @GetMapping("/{id}/contents")
    public ResponseEntity<List<FrontendContentDto>> getContents(@PathVariable Integer id) {
        return ResponseEntity.ok(itemService.getContentsForItem(id));
    }

    @PostMapping("/{id}/contents")
    public ResponseEntity<FrontendContentDto> createContent(
            @PathVariable Integer id,
            @RequestBody FrontendCreateContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createContentForItem(id, request));
    }
}
