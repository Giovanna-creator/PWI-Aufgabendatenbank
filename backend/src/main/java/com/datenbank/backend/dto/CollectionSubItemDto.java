package com.datenbank.backend.dto;

import java.util.UUID;

/**
 * DTO für ein SubItem einer Kollektion.
 * Enthält Position und optional die vollständigen Item-Daten.
 */
public class CollectionSubItemDto {

    private UUID subItemId;
    private Integer position;

    /**
     * Optional: vollständige Item-Daten eingebettet.
     * null wenn nicht benötigt.
     */
    private ItemResponseDto item;

    public CollectionSubItemDto() {}

    public UUID getSubItemId() { return subItemId; }
    public void setSubItemId(UUID id) { this.subItemId = id; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer p) { this.position = p; }

    public ItemResponseDto getItem() { return item; }
    public void setItem(ItemResponseDto item) { this.item = item; }
}