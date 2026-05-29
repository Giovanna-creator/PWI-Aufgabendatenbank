package com.datenbank.backend.controller;

import com.datenbank.backend.dto.TagCreateDto;
import com.datenbank.backend.dto.TagResponseDto;
import com.datenbank.backend.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Controller für die Tag-Entität.
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // GET /api/tags
    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAll() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    // GET /api/tags/roots
    @GetMapping("/roots")
    public ResponseEntity<List<TagResponseDto>> getRoots() {
        return ResponseEntity.ok(tagService.getRootTags());
    }

    // GET /api/tags/{id}/children
    @GetMapping("/{id}/children")
    public ResponseEntity<List<TagResponseDto>> getChildren(@PathVariable Integer id) {
        return ResponseEntity.ok(tagService.getChildTags(id));
    }

    // GET /api/tags/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    // POST /api/tags
    @PostMapping
    public ResponseEntity<TagResponseDto> create(
            @Valid @RequestBody TagCreateDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tagService.createTag(dto));
    }

    // PUT /api/tags/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody TagCreateDto dto) {

        return ResponseEntity.ok(tagService.updateTag(id, dto));
    }

    // DELETE /api/tags/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}