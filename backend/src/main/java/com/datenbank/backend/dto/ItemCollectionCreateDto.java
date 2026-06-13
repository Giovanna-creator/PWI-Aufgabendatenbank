package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO für das Erstellen und Aktualisieren einer Kollektion.
 *
 * Wird vom Frontend an das Backend gesendet bei:
 *  - POST /api/collections (Erstellung)
 *  - PUT  /api/collections/{id} (Aktualisierung)
 *
 * Enthält KEINE itemCollectionId — diese wird vom System vergeben.
 */
public class ItemCollectionCreateDto {

    /**
     * Optional: Verweis auf das übergeordnete Item (Eltern-Kollektion).
     */
    private UUID parentItemId;

    /**
     * Optional: Reihenfolge der Kollektion.
     * true = geordnet (SubItems haben Positionen 1, 2, 3...)
     * false = ungeordnet (Positionen null)
     */
    private Boolean order = false;

    /**
     * Liste der Aufgaben mit ihrer Position in der Kollektion.
     * Beim Erstellen leer lassen, falls keine Aufgaben zugewiesen werden.
     */
    @NotNull(message = "SubItems dürfen nicht null sein")
    private List<SubItemDto> subItems = new ArrayList<>();

    /**
     * Inneres DTO für jede Aufgabe in der Kollektion.
     */
    public static class SubItemDto {

        @NotNull(message = "SubItem ID ist Pflicht")
        private UUID subitemId;

        private Integer position;  // null erlaubt für ungeordnete Collections

        public UUID getSubitemId() { return subitemId; }
        public void setSubitemId(UUID id) { this.subitemId = id; }

        public Integer getPosition() { return position; }
        public void setPosition(Integer p) { this.position = p; }
    }

    public ItemCollectionCreateDto() {}

    public UUID getParentItemId() { return parentItemId; }
    public void setParentItemId(UUID id) { this.parentItemId = id; }

    public Boolean getOrder() { return order; }
    public void setOrder(Boolean o) { this.order = o; }

    public List<SubItemDto> getSubItems() { return subItems; }
    public void setSubItems(List<SubItemDto> s) { this.subItems = s; }
}