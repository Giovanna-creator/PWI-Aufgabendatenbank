package com.datenbank.backend.service;

import com.datenbank.backend.dto.ValidatorCreateDto;
import com.datenbank.backend.dto.ValidatorResponseDto;
import com.datenbank.backend.entity.Validator;
import com.datenbank.backend.repository.ValidatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTest {

    private static final UUID VALIDATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NOT_FOUND_ID = UUID.fromString("00000000-0000-0000-0000-00000000FFFF");

    @Mock
    private ValidatorRepository validatorRepository;

    @InjectMocks
    private ValidatorService service;

    private Validator validator;
    private ValidatorCreateDto createDto;

    @BeforeEach
    void setUp() {
        validator = new Validator("Must contain JOIN", "regex:.*JOIN.*");
        validator.setValidatorId(VALIDATOR_ID);

        createDto = new ValidatorCreateDto();
        createDto.setDescription("Must contain JOIN");
        createDto.setValidator("regex:.*JOIN.*");
    }

    @Test
    void getAll_returnsList() {
        when(validatorRepository.findAll()).thenReturn(List.of(validator));

        List<ValidatorResponseDto> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(VALIDATOR_ID, result.get(0).getValidatorId());
        assertEquals("Must contain JOIN", result.get(0).getDescription());
    }

    @Test
    void getAll_emptyList() {
        when(validatorRepository.findAll()).thenReturn(List.of());

        List<ValidatorResponseDto> result = service.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_returnsValidator() {
        when(validatorRepository.findById(VALIDATOR_ID)).thenReturn(Optional.of(validator));

        ValidatorResponseDto result = service.getById(VALIDATOR_ID);

        assertEquals(VALIDATOR_ID, result.getValidatorId());
        assertEquals("Must contain JOIN", result.getDescription());
        assertEquals("regex:.*JOIN.*", result.getValidator());
    }

    @Test
    void getById_notFound_throws404() {
        when(validatorRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getById(NOT_FOUND_ID));
    }

    @Test
    void create_createsValidator() {
        when(validatorRepository.save(any(Validator.class))).thenReturn(validator);

        ValidatorResponseDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals(VALIDATOR_ID, result.getValidatorId());
        assertEquals("Must contain JOIN", result.getDescription());
    }

    @Test
    void update_updatesValidator() {
        when(validatorRepository.findById(VALIDATOR_ID)).thenReturn(Optional.of(validator));
        when(validatorRepository.save(any(Validator.class))).thenReturn(validator);

        ValidatorResponseDto result = service.update(VALIDATOR_ID, createDto);

        assertNotNull(result);
        assertEquals("Must contain JOIN", result.getDescription());
        assertEquals("regex:.*JOIN.*", result.getValidator());
    }

    @Test
    void update_notFound_throws404() {
        when(validatorRepository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.update(NOT_FOUND_ID, createDto));
    }

    @Test
    void delete_deletes() {
        when(validatorRepository.existsById(VALIDATOR_ID)).thenReturn(true);

        service.delete(VALIDATOR_ID);

        verify(validatorRepository).deleteById(VALIDATOR_ID);
    }

    @Test
    void delete_notFound_throws404() {
        when(validatorRepository.existsById(NOT_FOUND_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.delete(NOT_FOUND_ID));
    }
}
