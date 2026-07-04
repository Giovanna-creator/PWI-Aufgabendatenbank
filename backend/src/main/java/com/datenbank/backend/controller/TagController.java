package com.datenbank.backend.controller;

import com.datenbank.backend.entity.Tag;
import com.datenbank.backend.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
        Tag parent = null;
        if (body.parentTagId() != null && !body.parentTagId().isBlank()) {
            parent = tagRepository.findById(UUID.fromString(body.parentTagId()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Eltern-Tag nicht gefunden"));
        }
        Tag saved = tagRepository.save(new Tag(body.tag().trim(), body.description(), parent));
        return toDto(saved);
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
