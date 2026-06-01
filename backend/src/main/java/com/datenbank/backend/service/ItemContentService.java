package com.datenbank.backend.service;

import com.datenbank.backend.dto.ItemContentCreateDto;
import com.datenbank.backend.dto.ItemContentResponseDto;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemContentService {

    private final ItemContentRepository contentRepository;
    private final AuthorRepository authorRepository;
    private final LicenseRepository licenseRepository;
    private final ItemContentTypeRepository contentTypeRepository;
    private final TagRepository tagRepository;

    public ItemContentService(
            ItemContentRepository contentRepository,
            AuthorRepository authorRepository,
            LicenseRepository licenseRepository,
            ItemContentTypeRepository contentTypeRepository,
            TagRepository tagRepository) {

        this.contentRepository = contentRepository;
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
    public ItemContentResponseDto getById(Integer id) {
        ItemContent content = findContentOrThrow(id);
        return convertToResponseDto(content);
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
            Integer id,
            ItemContentCreateDto dto) {

        ItemContent content = findContentOrThrow(id);

        applyDtoToEntity(dto, content);

        ItemContent saved = contentRepository.save(content);

        return convertToResponseDto(saved);
    }

    @Transactional
    public void delete(Integer id) {

        if (!contentRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Content nicht gefunden");
        }

        contentRepository.deleteById(id);
    }


    // Hilfmethoden


    private ItemContent findContentOrThrow(Integer id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Content nicht gefunden"));
    }

    private void applyDtoToEntity(
            ItemContentCreateDto dto,
            ItemContent content) {

        Author author = authorRepository
                .findById(dto.getAuthorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Author nicht gefunden"));

        License license = licenseRepository
                .findById(dto.getLicenseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "License nicht gefunden"));

        ItemContentType contentType = contentTypeRepository
                .findById(dto.getItemContentTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ItemContentType nicht gefunden"));

        content.setAuthor(author);
        content.setLicense(license);
        content.setItemContentType(contentType);

        content.setJsonSerializedContent(
                dto.getJsonSerializedContent());

        content.setBlobSerializedContent(
                dto.getBlobSerializedContent());

        // Tags
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {

            Set<Tag> tags = dto.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Tag nicht gefunden: " + tagId)))
                    .collect(Collectors.toSet());

            content.setTags(tags);

        } else {
            content.setTags(Set.of());
        }
    }

    private ItemContentResponseDto convertToResponseDto(
            ItemContent content) {

        ItemContentResponseDto dto =
                new ItemContentResponseDto();

        dto.setItemContentId(content.getItemContentId());

        // Author
        dto.setAuthorId(content.getAuthor().getAuthorId());
        dto.setAuthorDescriptor(
                content.getAuthor().getAuthorDescriptor());

        // License
        dto.setLicenseId(content.getLicense().getLicenseId());
        dto.setLicenseName(
                content.getLicense().getLicenseName());

        // ContentType
        dto.setItemContentTypeId(
                content.getItemContentType().getItemContentTypeId());

        dto.setItemContentTypeName(
                content.getItemContentType().getContentTypeName());

        // Content
        dto.setJsonSerializedContent(
                content.getJsonSerializedContent());

        dto.setBlobSerializedContent(
                content.getBlobSerializedContent());

        // Tags
        dto.setTagIds(
                content.getTags().stream()
                        .map(Tag::getTagId)
                        .collect(Collectors.toSet())
        );

        // Timestamps
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());

        return dto;
    }
}

