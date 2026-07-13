package com.datenbank.backend.controller;

import com.datenbank.backend.dto.*;
import com.datenbank.backend.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final UUID ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TAG_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID VALIDATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private ItemResponseDto createSampleItem() {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setItemId(ITEM_ID);
        dto.setAuthorId(UUID.randomUUID());
        dto.setLicenseId(UUID.randomUUID());
        dto.setItemTypeId(UUID.randomUUID());
        dto.setTagIds(Set.of(TAG_ID));
        dto.setCollection(false);
        return dto;
    }

    // ================================================================
    // GET /api/items
    // ================================================================

    @Test
    void getAll_returns200() throws Exception {
        when(itemService.getAllItems()).thenReturn(List.of(createSampleItem()));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].itemId").value(ITEM_ID.toString()));
    }

    @Test
    void getAll_withRootFilter_returnsRoots() throws Exception {
        when(itemService.getRootItems()).thenReturn(List.of(createSampleItem()));

        mockMvc.perform(get("/api/items?root=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll_withSearch_returnsFiltered() throws Exception {
        when(itemService.searchItems("test", null, null, null))
                .thenReturn(List.of(createSampleItem()));

        mockMvc.perform(get("/api/items?search=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ================================================================
    // GET /api/items/{id}
    // ================================================================

    @Test
    void getById_returns200() throws Exception {
        when(itemService.getItemById(ITEM_ID)).thenReturn(createSampleItem());

        mockMvc.perform(get("/api/items/{id}", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(ITEM_ID.toString()));
    }

    // ================================================================
    // POST /api/items
    // ================================================================

    @Test
    void create_returns201() throws Exception {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setAuthorId(UUID.randomUUID());
        dto.setLicenseId(UUID.randomUUID());
        dto.setItemTypeId(UUID.randomUUID());

        when(itemService.createItem(any(ItemCreateDto.class))).thenReturn(createSampleItem());

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemId").value(ITEM_ID.toString()));
    }

    // ================================================================
    // PUT /api/items/{id}
    // ================================================================

    @Test
    void update_returns200() throws Exception {
        ItemCreateDto dto = new ItemCreateDto();
        dto.setAuthorId(UUID.randomUUID());
        dto.setLicenseId(UUID.randomUUID());
        dto.setItemTypeId(UUID.randomUUID());

        when(itemService.updateItem(eq(ITEM_ID), any(ItemCreateDto.class)))
                .thenReturn(createSampleItem());

        mockMvc.perform(put("/api/items/{id}", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(ITEM_ID.toString()));
    }

    // ================================================================
    // DELETE /api/items/{id}
    // ================================================================

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(itemService).deleteItem(ITEM_ID);

        mockMvc.perform(delete("/api/items/{id}", ITEM_ID))
                .andExpect(status().isNoContent());
    }

    // ================================================================
    // POST /api/items/{id}/collection
    // ================================================================

    @Test
    void convertToCollection_returns200() throws Exception {
        when(itemService.convertToCollection(ITEM_ID)).thenReturn(createSampleItem());

        mockMvc.perform(post("/api/items/{id}/collection", ITEM_ID))
                .andExpect(status().isOk());
    }

    // ================================================================
    // POST /api/items/{id}/tags
    // ================================================================

    @Test
    void addTag_returns200() throws Exception {
        when(itemService.addTag(ITEM_ID, TAG_ID)).thenReturn(createSampleItem());

        mockMvc.perform(post("/api/items/{id}/tags", ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tagId\": \"" + TAG_ID + "\"}"))
                .andExpect(status().isOk());
    }

    // ================================================================
    // DELETE /api/items/{id}/tags/{tagId}
    // ================================================================

    @Test
    void removeTag_returns200() throws Exception {
        when(itemService.removeTag(ITEM_ID, TAG_ID)).thenReturn(createSampleItem());

        mockMvc.perform(delete("/api/items/{id}/tags/{tagId}", ITEM_ID, TAG_ID))
                .andExpect(status().isOk());
    }

    // ================================================================
    // GET /api/items/{id}/validators
    // ================================================================

    @Test
    void getValidatorsForItem_returns200() throws Exception {
        ValidatorResponseDto v = new ValidatorResponseDto();
        v.setValidatorId(VALIDATOR_ID);
        v.setDescription("Test Validator");
        v.setValidator("regex:.*");

        when(itemService.getValidatorsForItem(ITEM_ID)).thenReturn(List.of(v));

        mockMvc.perform(get("/api/items/{id}/validators", ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].validatorId").value(VALIDATOR_ID.toString()));
    }

    // ================================================================
    // POST /api/items/{id}/validators/{validatorId}
    // ================================================================

    @Test
    void addValidatorToItem_returns200() throws Exception {
        when(itemService.addValidatorToItem(ITEM_ID, VALIDATOR_ID))
                .thenReturn(createSampleItem());

        mockMvc.perform(post("/api/items/{id}/validators/{validatorId}",
                        ITEM_ID, VALIDATOR_ID))
                .andExpect(status().isOk());
    }

    // ================================================================
    // DELETE /api/items/{id}/validators/{validatorId}
    // ================================================================

    @Test
    void removeValidatorFromItem_returns204() throws Exception {
        when(itemService.removeValidatorFromItem(ITEM_ID, VALIDATOR_ID))
                .thenReturn(createSampleItem());

        mockMvc.perform(delete("/api/items/{id}/validators/{validatorId}",
                        ITEM_ID, VALIDATOR_ID))
                .andExpect(status().isNoContent());
    }
}
