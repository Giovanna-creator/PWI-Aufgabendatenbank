package com.datenbank.backend.dto;

/**
 * DTO für ein SubItem einer Kollektion.
 * Enthält Position und optional die vollständigen Item-Daten.
 */
public class CollectionSubItemDto {

    private Integer subItemId;
    private Integer position;

    /**
     * Optional: vollständige Item-Daten eingebettet.
     * null wenn nicht benötigt.
     */
    private ItemResponseDto item;

    public CollectionSubItemDto() {}

    public Integer getSubItemId() { return subItemId; }
    public void setSubItemId(Integer id) { this.subItemId = id; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer p) { this.position = p; }

    public ItemResponseDto getItem() { return item; }
    public void setItem(ItemResponseDto item) { this.item = item; }
}