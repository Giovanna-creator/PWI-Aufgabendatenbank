package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemContentCreateDto;
import com.datenbank.backend.dto.ItemContentResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ItemContentService {

    private final ItemContentRepository contentRepository;
    private final ItemContentsRepository itemContentsRepository;
    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemContentTypeRepository contentTypeRepository;
    private final TagRepository tagRepository;

    public ItemContentService(
            ItemContentRepository contentRepository,
            ItemContentsRepository itemContentsRepository,
            AuthorRepository authorRepository,
            LicenseRepository licenseRepository,
            ItemContentTypeRepository contentTypeRepository,
            TagRepository tagRepository) {

        this.contentRepository = contentRepository;
        this.itemContentsRepository = itemContentsRepository;
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.contentTypeRepository = contentTypeRepository;
        this.tagRepository = tagRepository;
    }


    // CRUD

    @Transactional(readOnly = true)
    public List<ItemContentResponseDto> getAll() {
        return contentRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemContentResponseDto getById(UUID id) {
        ItemContent content = findContentOrThrow(id);
        return convertToResponseDto(content);
    }

    /**
     * Liefert alle Contents eines Items (über item_contents Join-Tabelle).
     */
    @Transactional(readOnly = true)
    public List<ItemContentResponseDto> getContentsByItemId(UUID itemId) {
        return itemContentsRepository.findByItem_ItemId(itemId)
                .stream()
                .map(ic -> convertToResponseDto(ic.getItemContent()))
                .collect(Collectors.toList());
    }
    /**
     * Liefert die Blob-Daten eines Contents direkt als byte[].
     * Wirft 404 falls Content nicht gefunden.
     * Wirft 404 falls keine Blob-Daten vorhanden.
     */
    @Transactional(readOnly = true)
    public byte[] getBlobById(UUID id) {
        ItemContent content = findContentOrThrow(id);
        if (content.getBlobSerializedContent() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Keine Blob-Daten vorhanden");
        }
        return content.getBlobSerializedContent();
    }

    @Transactional
    public ItemContentResponseDto create(ItemContentCreateDto dto) {
        ItemContent content = new ItemContent();
        applyDtoToEntity(dto, content);

        ItemContent saved = contentRepository.save(content);

        return convertToResponseDto(saved);
    }

    @Transactional
    public ItemContentResponseDto update(
            UUID id,
            ItemContentCreateDto dto) {

        ItemContent content = findContentOrThrow(id);

        applyDtoToEntity(dto, content);

        ItemContent saved = contentRepository.save(content);

        return convertToResponseDto(saved);
    }

    /**
     * Speichert Blob-Daten für einen existierenden Content.
     * Wirft 404, falls Content nicht gefunden.
     */
    @Transactional
    public ItemContentResponseDto uploadBlob(UUID id, byte[] blob) {
        ItemContent content = findContentOrThrow(id);
        content.setBlobSerializedContent(blob);
        ItemContent saved = contentRepository.save(content);
        return convertToResponseDto(saved);
    }

    @Transactional
    public void delete(UUID id) {

        if (!contentRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Content nicht gefunden");
        }

        contentRepository.deleteById(id);
    }


    // Hilfsmethoden


    private ItemContent findContentOrThrow(UUID id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Content nicht gefunden"));
    }

    private void applyDtoToEntity(ItemContentCreateDto dto, ItemContent content) {

        // Pflicht-Beziehungen (NOT NULL in Entity)
        content.setAuthor(authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Author nicht gefunden")));

        content.setLicense(licenseRepository.findById(dto.getLicenseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "License nicht gefunden")));

        content.setItemContentType(
                contentTypeRepository.findById(dto.getItemContentTypeId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "ContentType nicht gefunden")));

        // Optionaler JSON-Inhalt
        content.setJsonSerializedContent(dto.getJsonSerializedContent());

        // Optionaler Blob-Inhalt (Bild, PDF)
        content.setBlobSerializedContent(dto.getBlobSerializedContent());

        // Many-to-Many: Tags
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(
                    tagRepository.findAllById(dto.getTagIds()));
            if (tags.size() != dto.getTagIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mindestens ein Tag wurde nicht gefunden");
            }
            content.setTags(tags);
        } else {
            content.setTags(new HashSet<>());
        }
    }

    /**
     * Wandelt eine ItemContent-Entity in ein ResponseDTO um.
     * Blob-Daten werden NICHT eingebettet — nur ein Flag ob vorhanden.
     */
    private ItemContentResponseDto convertToResponseDto(ItemContent content) {
        ItemContentResponseDto dto = new ItemContentResponseDto();

        dto.setItemContentId(content.getItemContentId());

        // Author
        if (content.getAuthor() != null) {
            dto.setAuthorId(content.getAuthor().getAuthorId());
            dto.setAuthorDescriptor(content.getAuthor().getDescriptor());
        }

        // License
        if (content.getLicense() != null) {
            dto.setLicenseId(content.getLicense().getLicenseId());
            dto.setLicenseName(content.getLicense().getLicense());
        }

        // ContentType
        if (content.getItemContentType() != null) {
            dto.setItemContentTypeId(
                    content.getItemContentType().getItemContentTypeId());
            dto.setItemContentTypeName(
                    content.getItemContentType().getItemContentTypeName());
        }

        // JSON-Inhalt direkt zurückgeben
        dto.setJsonSerializedContent(content.getJsonSerializedContent());

        // Blob: nur Flag setzen, nicht den Inhalt
        dto.setHasBlobContent(content.getBlobSerializedContent() != null);

        // Tags als IDs
        dto.setTagIds(content.getTags().stream()
                .map(Tag::getTagId)
                .collect(Collectors.toSet()));

        // Timestamps
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());

        return dto;
    }
}

