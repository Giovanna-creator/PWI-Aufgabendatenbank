package com.datenbank.backend.service;

import com.datenbank.backend.dto.ValidatorCreateDto;
import com.datenbank.backend.dto.ValidatorResponseDto;
import com.datenbank.backend.entity.Validator;
import com.datenbank.backend.repository.ValidatorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidatorService {

    private final ValidatorRepository validatorRepository;

    public ValidatorService(ValidatorRepository validatorRepository) {
        this.validatorRepository = validatorRepository;
    }

    @Transactional(readOnly = true)
    public List<ValidatorResponseDto> getAll() {
        return validatorRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ValidatorResponseDto getById(UUID id) {
        return convertToResponseDto(findOrThrow(id));
    }

    @Transactional
    public ValidatorResponseDto create(ValidatorCreateDto dto) {
        Validator validator = new Validator(dto.getDescription(), dto.getValidator());
        Validator saved = validatorRepository.save(validator);
        return convertToResponseDto(saved);
    }

    @Transactional
    public ValidatorResponseDto update(UUID id, ValidatorCreateDto dto) {
        Validator validator = findOrThrow(id);
        validator.setDescription(dto.getDescription());
        validator.setValidator(dto.getValidator());
        Validator saved = validatorRepository.save(validator);
        return convertToResponseDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        if (!validatorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Validator nicht gefunden");
        }
        validatorRepository.deleteById(id);
    }

    private Validator findOrThrow(UUID id) {
        return validatorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Validator nicht gefunden"));
    }

    private ValidatorResponseDto convertToResponseDto(Validator validator) {
        ValidatorResponseDto dto = new ValidatorResponseDto();
        dto.setValidatorId(validator.getValidatorId());
        dto.setDescription(validator.getDescription());
        dto.setValidator(validator.getValidator());
        return dto;
    }
}
