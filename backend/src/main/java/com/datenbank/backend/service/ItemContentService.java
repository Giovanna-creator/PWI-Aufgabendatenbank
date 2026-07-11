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
    private final ItemRepository itemRepository;
    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemContentTypeRepository contentTypeRepository;
    private final TagRepository tagRepository;

    public ItemContentService(
            ItemContentRepository contentRepository,
            ItemContentsRepository itemContentsRepository,
            ItemRepository itemRepository,
            AuthorRepository authorRepository,
            LicenseRepository licenseRepository,
            ItemContentTypeRepository contentTypeRepository,
            TagRepository tagRepository) {

        this.contentRepository = contentRepository;
        this.itemContentsRepository = itemContentsRepository;
        this.itemRepository = itemRepository;
        this.authorRepository = authorRepository;
        this.licenseRepository = licenseRepository;
        this.contentTypeRepository = contentTypeRepository;
        this.tagRepository = tagRepository;
    }


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

    @Transactional(readOnly = true)
    public List<ItemContentResponseDto> getContentsByItemId(UUID itemId) {
        return itemContentsRepository.findByItem_ItemId(itemId)
                .stream()
                .map(ic -> {
                    ItemContentResponseDto dto = convertToResponseDto(ic.getItemContent());
                    // purpose lebt im Join, nicht im ItemContent selbst
                    dto.setPurpose(ic.getPurpose());
                    return dto;
                })
                .collect(Collectors.toList());
    }
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

    /** Purpose (z. B. "Aufgabenstellung") wird im item_contents-Join gespeichert. */
    @Transactional
    public ItemContentResponseDto createForItem(UUID itemId, ItemContentCreateDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item nicht gefunden"));

        ItemContent content = new ItemContent();
        applyDtoToEntity(dto, content);
        ItemContent saved = contentRepository.save(content);

        ItemContents link = new ItemContents(item, saved, dto.getPurpose());
        itemContentsRepository.save(link);

        return convertToResponseDto(saved);
    }

    @Transactional
    public ItemContentResponseDto update(
            UUID id,
            ItemContentCreateDto dto) {

        ItemContent content = findContentOrThrow(id);

        applyDtoToEntity(dto, content);

        ItemContent saved = contentRepository.save(content);

        // purpose lebt im item_contents-Join: nur aktualisieren wenn mitgeschickt.
        // In dieser Iteration ist ein Content genau einem Item zugeordnet.
        if (dto.getPurpose() != null) {
            List<ItemContents> links =
                    itemContentsRepository.findByItemContent_ItemContentId(id);
            links.forEach(link -> link.setPurpose(dto.getPurpose()));
            itemContentsRepository.saveAll(links);
        }

        return convertToResponseDto(saved);
    }

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

    /** Blob-Daten werden NICHT eingebettet — nur ein Flag ob vorhanden. */
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

