package com.datenbank.backend.service;

import com.datenbank.backend.dto.ReprTemplateCreateDto;
import com.datenbank.backend.dto.ReprTemplateResponseDto;
import com.datenbank.backend.entity.ItemRepresentationTemplate;
import com.datenbank.backend.repository.ItemRepresentationTemplateRepository;
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
class RepresentationTemplateServiceTest {

    private static final UUID TEMPLATE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NOT_FOUND_ID = UUID.fromString("00000000-0000-0000-0000-00000000FFFF");

    @Mock
    private ItemRepresentationTemplateRepository repository;

    @InjectMocks
    private RepresentationTemplateService service;

    private ItemRepresentationTemplate template;
    private ReprTemplateCreateDto createDto;

    @BeforeEach
    void setUp() {
        template = new ItemRepresentationTemplate("<xml>content</xml>");
        template.setItemTemplateId(TEMPLATE_ID);

        createDto = new ReprTemplateCreateDto();
        createDto.setTemplate("<xml>content</xml>");
    }

    @Test
    void getAll_returnsList() {
        when(repository.findAll()).thenReturn(List.of(template));

        List<ReprTemplateResponseDto> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(TEMPLATE_ID, result.get(0).getId());
        assertEquals("<xml>content</xml>", result.get(0).getTemplate());
    }

    @Test
    void getAll_emptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<ReprTemplateResponseDto> result = service.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getById_returnsTemplate() {
        when(repository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        ReprTemplateResponseDto result = service.getById(TEMPLATE_ID);

        assertEquals(TEMPLATE_ID, result.getId());
        assertEquals("<xml>content</xml>", result.getTemplate());
    }

    @Test
    void getById_notFound_throws404() {
        when(repository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.getById(NOT_FOUND_ID));
    }

    @Test
    void create_createsTemplate() {
        when(repository.save(any(ItemRepresentationTemplate.class))).thenReturn(template);

        ReprTemplateResponseDto result = service.create(createDto);

        assertNotNull(result);
        assertEquals(TEMPLATE_ID, result.getId());
        assertEquals("<xml>content</xml>", result.getTemplate());
    }

    @Test
    void update_updatesTemplate() {
        when(repository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(repository.save(any(ItemRepresentationTemplate.class))).thenReturn(template);

        ReprTemplateResponseDto result = service.update(TEMPLATE_ID, createDto);

        assertNotNull(result);
        assertEquals("<xml>content</xml>", result.getTemplate());
    }

    @Test
    void update_notFound_throws404() {
        when(repository.findById(NOT_FOUND_ID)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> service.update(NOT_FOUND_ID, createDto));
    }

    @Test
    void delete_deletes() {
        when(repository.existsById(TEMPLATE_ID)).thenReturn(true);

        service.delete(TEMPLATE_ID);

        verify(repository).deleteById(TEMPLATE_ID);
    }

    @Test
    void delete_notFound_throws404() {
        when(repository.existsById(NOT_FOUND_ID)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.delete(NOT_FOUND_ID));
    }
}
