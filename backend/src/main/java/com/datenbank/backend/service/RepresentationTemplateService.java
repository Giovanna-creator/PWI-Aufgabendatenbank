package com.datenbank.backend.service;

import com.datenbank.backend.dto.ReprTemplateCreateDto;
import com.datenbank.backend.dto.ReprTemplateResponseDto;
import com.datenbank.backend.entity.ItemRepresentationTemplate;
import com.datenbank.backend.repository.ItemRepresentationTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RepresentationTemplateService {

    private final ItemRepresentationTemplateRepository repository;

    public RepresentationTemplateService(ItemRepresentationTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ReprTemplateResponseDto> getAll() {
        return repository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReprTemplateResponseDto getById(UUID id) {
        return convertToResponseDto(findOrThrow(id));
    }

    @Transactional
    public ReprTemplateResponseDto create(ReprTemplateCreateDto dto) {
        ItemRepresentationTemplate entity = new ItemRepresentationTemplate(dto.getTemplate());
        ItemRepresentationTemplate saved = repository.save(entity);
        return convertToResponseDto(saved);
    }

    @Transactional
    public ReprTemplateResponseDto update(UUID id, ReprTemplateCreateDto dto) {
        ItemRepresentationTemplate entity = findOrThrow(id);
        entity.setTemplate(dto.getTemplate());
        ItemRepresentationTemplate saved = repository.save(entity);
        return convertToResponseDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template nicht gefunden");
        }
        repository.deleteById(id);
    }

    private ItemRepresentationTemplate findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Template nicht gefunden"));
    }

    private ReprTemplateResponseDto convertToResponseDto(ItemRepresentationTemplate entity) {
        ReprTemplateResponseDto dto = new ReprTemplateResponseDto();
        dto.setId(entity.getItemTemplateId());
        dto.setTemplate(entity.getTemplate());
        return dto;
    }
}
