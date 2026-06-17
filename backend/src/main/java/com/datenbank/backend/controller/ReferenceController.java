package com.datenbank.backend.controller;

import com.datenbank.backend.repository.AuthorRepository;
import com.datenbank.backend.repository.ItemContentTypeRepository;
import com.datenbank.backend.repository.ItemTypeRepository;
import com.datenbank.backend.repository.LicenseRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only Endpunkte für die Referenzdaten (Lookup-Listen).
 *
 * Damit das Frontend Autor, Lizenz, Aufgaben-Typ und Content-Typ in
 * Dropdowns anbieten kann, statt feste SEED-UUIDs hartzucodieren.
 * Bewusst nur GET — diese Listen werden in dieser Iteration nicht über
 * die UI gepflegt.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ReferenceController {

    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final ItemContentTypeRepository contentTypeRepository;

    public ReferenceController(AuthorRepository authorRepository,
                               LicenseRepository licenseRepository,
                               ItemTypeRepository itemTypeRepository,
                               ItemContentTypeRepository contentTypeRepository) {
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.itemTypeRepository = itemTypeRepository;
        this.contentTypeRepository = contentTypeRepository;
    }

    // Schlanke DTOs (nur ID + Anzeigename) — ideal für Dropdowns.
    public record AuthorDto(String id, String descriptor, String mail) {}
    public record LicenseDto(String id, String name) {}
    public record ItemTypeDto(String id, String name, String description) {}
    public record ContentTypeDto(String id, String name, String description) {}

    @GetMapping("/authors")
    public List<AuthorDto> getAuthors() {
        return authorRepository.findAll().stream()
                .map(a -> new AuthorDto(a.getAuthorId().toString(), a.getDescriptor(), a.getMail()))
                .toList();
    }

    @GetMapping("/licenses")
    public List<LicenseDto> getLicenses() {
        return licenseRepository.findAll().stream()
                .map(l -> new LicenseDto(l.getLicenseId().toString(), l.getLicense()))
                .toList();
    }

    @GetMapping("/item-types")
    public List<ItemTypeDto> getItemTypes() {
        return itemTypeRepository.findAll().stream()
                .map(t -> new ItemTypeDto(
                        t.getItemTypeId().toString(), t.getItemTypeName(), t.getDescription()))
                .toList();
    }

    @GetMapping("/content-types")
    public List<ContentTypeDto> getContentTypes() {
        return contentTypeRepository.findAll().stream()
                .map(c -> new ContentTypeDto(
                        c.getItemContentTypeId().toString(),
                        c.getItemContentTypeName(),
                        c.getDescription()))
                .toList();
    }
}
