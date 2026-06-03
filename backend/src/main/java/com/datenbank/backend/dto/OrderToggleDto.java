package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO für das Umschalten des Order-Flags einer Kollektion.
 *
 * Wird verwendet bei:
 *  - PUT /api/collections/{id}/order
 *
 * Beispiel-Body: { "order": true }
 */
public class OrderToggleDto {

    @NotNull(message = "order ist Pflicht")
    private Boolean order;

    public OrderToggleDto() {}

    public Boolean getOrder() {
        return order;
    }

    public void setOrder(Boolean order) {
        this.order = order;
    }
}