package com.datenbank.backend.controller;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.service.ItemContentService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@CrossOrigin(origins = "*")
public class ItemContentController {

    private final ItemContentService contentService;

    public ItemContentController(ItemContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/{id}/blob")
    public ResponseEntity<byte[]> getBlob(@PathVariable Integer id) {
        byte[] blob = contentService.getBlobById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(blob);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FrontendContentDto> update(
            @PathVariable Integer id,
            @RequestBody FrontendCreateContentRequest request) {
        return ResponseEntity.ok(contentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        contentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
