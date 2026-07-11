package com.datenbank.backend.controller;

import com.datenbank.backend.entity.Author;
import com.datenbank.backend.entity.ItemContentType;
import com.datenbank.backend.entity.ItemType;
import com.datenbank.backend.entity.License;
import com.datenbank.backend.repository.AuthorRepository;
import com.datenbank.backend.repository.ItemContentTypeRepository;
import com.datenbank.backend.repository.ItemTypeRepository;
import com.datenbank.backend.repository.LicenseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    // Request-Bodies zum Anlegen neuer Referenzdaten.
    public record AuthorCreate(String descriptor, String mail) {}
    public record LicenseCreate(String name) {}
    public record ItemTypeCreate(String name, String description) {}
    public record ContentTypeCreate(String name, String description) {}

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



    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDto createAuthor(@RequestBody AuthorCreate body) {
        Author a = authorRepository.save(new Author(body.descriptor(), body.mail()));
        return new AuthorDto(a.getAuthorId().toString(), a.getDescriptor(), a.getMail());
    }

    @PostMapping("/licenses")
    @ResponseStatus(HttpStatus.CREATED)
    public LicenseDto createLicense(@RequestBody LicenseCreate body) {
        License l = licenseRepository.save(new License(body.name()));
        return new LicenseDto(l.getLicenseId().toString(), l.getLicense());
    }

    @PostMapping("/item-types")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemTypeDto createItemType(@RequestBody ItemTypeCreate body) {
        ItemType t = itemTypeRepository.save(new ItemType(body.name(), body.description()));
        return new ItemTypeDto(t.getItemTypeId().toString(), t.getItemTypeName(), t.getDescription());
    }

    @PostMapping("/content-types")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentTypeDto createContentType(@RequestBody ContentTypeCreate body) {
        ItemContentType c = contentTypeRepository.save(
                new ItemContentType(body.name(), body.description()));
        return new ContentTypeDto(
                c.getItemContentTypeId().toString(), c.getItemContentTypeName(), c.getDescription());
    }
}
