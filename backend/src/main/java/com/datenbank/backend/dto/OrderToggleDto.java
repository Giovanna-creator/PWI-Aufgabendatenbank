package com.datenbank.backend.dto;

/**
 * DTO zum Umschalten der Reihenfolge einer Kollektion.
 *
 * PUT /api/collections/{id}/order
 * { "order": true }  → geordnet (Positionen 1, 2, 3...)
 * { "order": false } → ungeordnet (Positionen null)
 */
public class OrderToggleDto {

    private Boolean order;

    public OrderToggleDto() {}

    public Boolean getOrder() { return order; }
    public void setOrder(Boolean order) { this.order = order; }
}
