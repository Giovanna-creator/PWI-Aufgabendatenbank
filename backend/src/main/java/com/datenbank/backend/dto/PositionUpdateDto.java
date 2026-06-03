package com.datenbank.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO für die Aktualisierung der Position eines SubItems
 * in einer geordneten Kollektion.
 *
 * Wird verwendet bei:
 *  - PUT /api/collections/{collectionId}/items/{itemId}/position
 *
 * Beispiel-Body: { "position": 3 }
 *
 * Position ist 1-basiert (erste Position ist 1, nicht 0).
 */
public class PositionUpdateDto {

    @NotNull(message = "position ist Pflicht")
    @Min(value = 1, message = "Position muss >= 1 sein")
    private Integer position;

    public PositionUpdateDto() {}

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}