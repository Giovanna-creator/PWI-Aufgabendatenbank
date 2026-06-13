package com.datenbank.backend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO für die Rückgabe einer Kollektion an das Frontend.
 *
 * Wird vom Backend an das Frontend gesendet bei:
 *  - GET  /api/collections
 *  - GET  /api/collections/{id}
 *  - POST /api/collections (Antwort nach Erstellung)
 *  - PUT  /api/collections/{id} (Antwort nach Aktualisierung)
 *
 * Enthält die itemCollectionId und den generierten Timestamp.
 * Sub-Items werden mit ID und Position zurückgegeben.
 */
public class ItemCollectionResponseDto {

    private UUID itemCollectionId;

    // Optionales Eltern-Item
    private UUID parentItemId;

    // Reihenfolge der Kollektion: true = geordnet, false = ungeordnet
    private Boolean order;

    /**
     * Aufgaben in dieser Kollektion, sortiert nach Position.
     */
    private List<SubItemResponseDto> subItems = new ArrayList<>();

    // Timestamp
    private LocalDateTime createdAt;

    

    
    //Anzahl der SubItems in dieser Kollektion.
    private Integer subItemCount;

    

    /**
     * Inneres DTO für jede Aufgabe in der Antwort.
     * Gibt ID und Position zurück.
     */
    public static class SubItemResponseDto {
        private UUID subitemId;
        private Integer position;

        public UUID getSubitemId() { return subitemId; }
        public void setSubitemId(UUID id) { this.subitemId = id; }

        public Integer getPosition() { return position; }
        public void setPosition(Integer p) { this.position = p; }
    }

    public ItemCollectionResponseDto() {}

    public UUID getItemCollectionId() { return itemCollectionId; }
    public void setItemCollectionId(UUID id) { this.itemCollectionId = id; }

    public UUID getParentItemId() { return parentItemId; }
    public void setParentItemId(UUID id) { this.parentItemId = id; }

    public Boolean getOrder() { return order; }
    public void setOrder(Boolean o) { this.order = o; }

    public List<SubItemResponseDto> getSubItems() { return subItems; }
    public void setSubItems(List<SubItemResponseDto> s) { this.subItems = s; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    // Getter & Setter
    public Integer getSubItemCount() { return subItemCount; }
    public void setSubItemCount(Integer c) { this.subItemCount = c; }
}