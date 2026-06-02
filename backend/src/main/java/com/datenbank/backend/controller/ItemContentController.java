package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ItemContentCreateDto;
import com.datenbank.backend.dto.ItemContentResponseDto;
import com.datenbank.backend.service.ItemContentService;

import jakarta.validation.Valid;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    /**
     * Liefert alle Contents.
     * GET /api/contents → 200 OK
     */

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

    /**
     * Liefert die Blob-Daten eines Contents (Bild, PDF).
     * GET /api/contents/{id}/blob → 200 OK oder 404
     *
     * Gibt die rohen Binärdaten zurück mit korrektem Content-Type.
     */
    @GetMapping("/{id}/blob")
    public ResponseEntity<byte[]> getBlob(@PathVariable Integer id) {
        byte[] blob = contentService.getBlobById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(blob);
    }

    // POST
    /**
     * Erstellt einen neuen Content.
     * POST /api/contents → 201 Created
     */

    @PostMapping
    public ResponseEntity<ItemContentResponseDto> create(
            @Valid @RequestBody ItemContentCreateDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contentService.create(dto));
    }

    /**
     * Lädt eine Datei (Bild, PDF) als Blob hoch.
     * POST /api/contents/{id}/blob → 200 OK
     *
     * Beispiel mit curl:
     * curl -X POST /api/contents/1/blob -F "file=@bild.png"
     */
    @PostMapping("/{id}/blob")
    public ResponseEntity<ItemContentResponseDto> uploadBlob(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(
                contentService.uploadBlob(id, file.getBytes()));
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

