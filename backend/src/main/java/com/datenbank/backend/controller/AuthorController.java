package com.datenbank.backend.controller;

import com.datenbank.backend.dto.AuthorCreateDto;
import com.datenbank.backend.dto.AuthorResponseDto;
import com.datenbank.backend.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // GET /api/authors
    @GetMapping
    public ResponseEntity<List<AuthorResponseDto>> getAll() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    // GET /api/authors/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    // POST /api/authors
    @PostMapping
    public ResponseEntity<AuthorResponseDto> create(
            @Valid @RequestBody AuthorCreateDto dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authorService.createAuthor(dto));
    }

    // PUT /api/authors/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> update(
            @PathVariable Integer id,
            @Valid @RequestBody AuthorCreateDto dto) {

        return ResponseEntity.ok(authorService.updateAuthor(id, dto));
    }

    // DELETE /api/authors/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}