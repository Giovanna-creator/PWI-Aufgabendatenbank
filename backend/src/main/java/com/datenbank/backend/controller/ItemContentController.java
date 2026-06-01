package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ItemContentCreateDto;
import com.datenbank.backend.dto.ItemContentResponseDto;
import com.datenbank.backend.service.ItemContentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für ItemContent.
 */
@RestController
@RequestMapping("/api/contents")
@CrossOrigin(origins = "*")
public class ItemContentController {

    private final ItemContentService contentService;

    public ItemContentController(
            ItemContentService contentService) {

        this.contentService = contentService;
    }


    // GET ALL


    @GetMapping
    public ResponseEntity<List<ItemContentResponseDto>> getAll() {

        return ResponseEntity.ok(
                contentService.getAll());
    }


    // GET BY ID


    @GetMapping("/{id}")
    public ResponseEntity<ItemContentResponseDto> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                contentService.getById(id));
    }


    // POST


    @PostMapping
    public ResponseEntity<ItemContentResponseDto> create(
            @Valid @RequestBody ItemContentCreateDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentService.create(dto));
    }


    // PUT


    @PutMapping("/{id}")
    public ResponseEntity<ItemContentResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody ItemContentCreateDto dto) {

        return ResponseEntity.ok(
                contentService.update(id, dto));
    }


    // DELETE


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        contentService.delete(id);

        return ResponseEntity.noContent().build();
    }
}

