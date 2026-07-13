package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ItemCreateDto;
import com.datenbank.backend.dto.ItemResponseDto;
import com.datenbank.backend.dto.ValidatorResponseDto;
import com.datenbank.backend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<ItemResponseDto> create(@Valid @RequestBody ItemCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ItemCreateDto dto) {
        return ResponseEntity.ok(itemService.updateItem(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/collection")
    public ResponseEntity<ItemResponseDto> convertToCollection(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.convertToCollection(id));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAll(
            @RequestParam(required = false) Boolean root,
            @RequestParam(required = false) UUID rootItemId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) UUID itemTypeId,
            @RequestParam(required = false) String tag) {

        // Such-/Filterparameter haben Vorrang
        if (hasText(search) || authorId != null || itemTypeId != null || hasText(tag)) {
            return ResponseEntity.ok(itemService.searchItems(search, authorId, itemTypeId, tag));
        }

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

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    @GetMapping("/{itemId}/validators")
    public ResponseEntity<List<ValidatorResponseDto>> getValidatorsForItem(
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(itemService.getValidatorsForItem(itemId));
    }

    @PostMapping("/{itemId}/validators/{validatorId}")
    public ResponseEntity<ItemResponseDto> addValidatorToItem(
            @PathVariable UUID itemId,
            @PathVariable UUID validatorId) {
        return ResponseEntity.ok(itemService.addValidatorToItem(itemId, validatorId));
    }

    @DeleteMapping("/{itemId}/validators/{validatorId}")
    public ResponseEntity<Void> removeValidatorFromItem(
            @PathVariable UUID itemId,
            @PathVariable UUID validatorId) {
        itemService.removeValidatorFromItem(itemId, validatorId);
        return ResponseEntity.noContent().build();
    }

    public record TagAssign(UUID tagId) {}

    @PostMapping("/{id}/tags")
    public ResponseEntity<ItemResponseDto> addTag(@PathVariable UUID id, @RequestBody TagAssign body) {
        return ResponseEntity.ok(itemService.addTag(id, body.tagId()));
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<ItemResponseDto> removeTag(@PathVariable UUID id, @PathVariable UUID tagId) {
        return ResponseEntity.ok(itemService.removeTag(id, tagId));
    }
}