package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ReprTemplateCreateDto;
import com.datenbank.backend.dto.ReprTemplateResponseDto;
import com.datenbank.backend.service.RepresentationTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/representation-templates")
@CrossOrigin(origins = "*")
public class RepresentationTemplateController {

    private final RepresentationTemplateService service;

    public RepresentationTemplateController(RepresentationTemplateService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ReprTemplateResponseDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReprTemplateResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ReprTemplateResponseDto> create(
            @Valid @RequestBody ReprTemplateCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReprTemplateResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReprTemplateCreateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
