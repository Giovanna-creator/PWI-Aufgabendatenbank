package com.datenbank.backend.controller;

import com.datenbank.backend.dto.ValidatorCreateDto;
import com.datenbank.backend.dto.ValidatorResponseDto;
import com.datenbank.backend.service.ValidatorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/validators")
@CrossOrigin(origins = "*")
public class ValidatorController {

    private final ValidatorService validatorService;

    public ValidatorController(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @GetMapping
    public ResponseEntity<List<ValidatorResponseDto>> getAll() {
        return ResponseEntity.ok(validatorService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValidatorResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(validatorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ValidatorResponseDto> create(
            @Valid @RequestBody ValidatorCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(validatorService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ValidatorResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ValidatorCreateDto dto) {
        return ResponseEntity.ok(validatorService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        validatorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
