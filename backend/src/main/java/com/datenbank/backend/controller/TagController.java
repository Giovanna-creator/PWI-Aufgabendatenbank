package com.datenbank.backend.controller;

import com.datenbank.backend.entity.Tag;
import com.datenbank.backend.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Endpunkte für die hierarchischen Tags.
 *
 * Tags bilden über parent_tag_id einen Baum. Das Frontend lädt die flache
 * Liste (inkl. parentTagId) und baut daraus die Pfade/den Baum selbst.
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public record TagDto(String id, String tag, String description, String parentTagId) {}
    public record TagCreate(String tag, String description, String parentTagId) {}

    @GetMapping
    public List<TagDto> getTags() {
        return tagRepository.findAll().stream().map(TagController::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagDto createTag(@RequestBody TagCreate body) {
        if (body.tag() == null || body.tag().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag-Name fehlt");
        }
        String name = body.tag().trim();
        if (name.matches(".*\\s.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag darf keine Leerzeichen enthalten");
        }
        if (tagRepository.existsByTagIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag existiert bereits");
        }
        Tag parent = null;
        if (body.parentTagId() != null && !body.parentTagId().isBlank()) {
            parent = tagRepository.findById(UUID.fromString(body.parentTagId()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Eltern-Tag nicht gefunden"));
        }
        Tag saved = tagRepository.save(new Tag(name, body.description(), parent));
        return toDto(saved);
    }

    /**
     * Löscht einen Tag. Die Datenbank kümmert sich um die Folgen:
     * Unter-Tags werden zu obersten Tags (parent_tag_id ON DELETE SET NULL),
     * Zuordnungen zu Aufgaben/Inhalten werden entfernt (ON DELETE CASCADE).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag nicht gefunden");
        }
        tagRepository.deleteById(id);
    }

    private static TagDto toDto(Tag t) {
        return new TagDto(
                t.getTagId().toString(),
                t.getTag(),
                t.getDescription(),
                t.getParentTag() != null ? t.getParentTag().getTagId().toString() : null
        );
    }
}
