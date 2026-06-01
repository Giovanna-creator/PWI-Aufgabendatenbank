package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

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
    private Integer parentItemId;

    /**
     * Optional: Reihenfolge der Kollektion.
     * Hinweis: "order" ist reserviert in SQL → collectionOrder.
     */
    private Integer collectionOrder;

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
        private Integer subitemId;

        @NotNull(message = "Position ist Pflicht")
        private Integer position;

        public Integer getSubitemId() { return subitemId; }
        public void setSubitemId(Integer id) { this.subitemId = id; }

        public Integer getPosition() { return position; }
        public void setPosition(Integer p) { this.position = p; }
    }

    public ItemCollectionCreateDto() {}

    public Integer getParentItemId() { return parentItemId; }
    public void setParentItemId(Integer id) { this.parentItemId = id; }

    public Integer getCollectionOrder() { return collectionOrder; }
    public void setCollectionOrder(Integer o) { this.collectionOrder = o; }

    public List<SubItemDto> getSubItems() { return subItems; }
    public void setSubItems(List<SubItemDto> s) { this.subItems = s; }
}