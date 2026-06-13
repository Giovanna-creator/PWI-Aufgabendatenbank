package com.datenbank.backend.service;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.entity.*;
import com.datenbank.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ItemContentService {

    private final ItemContentRepository contentRepository;
    private final ItemContentTypeRepository contentTypeRepository;

    public ItemContentService(ItemContentRepository contentRepository,
                               ItemContentTypeRepository contentTypeRepository) {
        this.contentRepository = contentRepository;
        this.contentTypeRepository = contentTypeRepository;
    }

    @Transactional(readOnly = true)
    public FrontendContentDto getById(Integer id) {
        ItemContent content = findContentOrThrow(id);
        String purpose = content.getItemContentId() != null ? "Aufgabenstellung" : null;
        return FrontendDtoMapper.toContentDto(content, purpose);
    }

    @Transactional(readOnly = true)
    public byte[] getBlobById(Integer id) {
        ItemContent content = findContentOrThrow(id);
        if (content.getBlobSerializedContent() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Keine Blob-Daten vorhanden");
        }
        return content.getBlobSerializedContent();
    }

    @Transactional
    public FrontendContentDto update(Integer id, FrontendCreateContentRequest request) {
        ItemContent content = findContentOrThrow(id);

        if (request.getContentType() != null) {
            ItemContentType contentType = contentTypeRepository
                    .findByItemContentTypeName(request.getContentType())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "ContentType '" + request.getContentType() + "' nicht gefunden"));
            content.setItemContentType(contentType);
        }

        content.setJsonSerializedContent(request.getJsonContent());
        if (request.getBlobContent() != null) {
            content.setBlobSerializedContent(request.getBlobContent().getBytes());
        }

        ItemContent saved = contentRepository.save(content);
        return FrontendDtoMapper.toContentDto(saved, request.getPurpose());
    }

    @Transactional
    public void delete(Integer id) {
        if (!contentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Content nicht gefunden");
        }
        contentRepository.deleteById(id);
    }

    private ItemContent findContentOrThrow(Integer id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Content nicht gefunden"));
    }
}
