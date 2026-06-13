package com.datenbank.backend.dto;

/**
 * DTO zum Aktualisieren der Position eines SubItems in einer Kollektion.
 *
 * PUT /api/collections/{id}/items/{itemId}/position
 * { "position": 2 } → Setzt Position auf 2, Geschwister werden neu berechnet
 * { "position": null } → Entfernt aus der Reihenfolge
 */
public class PositionUpdateDto {

    private Integer position;

    public PositionUpdateDto() {}

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
