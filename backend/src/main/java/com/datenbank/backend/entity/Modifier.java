package com.datenbank.backend.entity;

import jakarta.persistence.*;

/**
 * Modifikatoren für Aufgaben-Varianten (z.B. "Variante mit INNER JOIN-Pflicht").
 */
@Entity
@Table(name = "modifier")
public class Modifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "modifier_id")
    private Integer  modifierId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "modifier", nullable = false, columnDefinition = "TEXT")
    private String modifier;

    public Modifier() {
    }

    public Modifier(String description, String modifier) {
        this.description = description;
        this.modifier = modifier;
    }

    public Integer getModifierId() {
        return modifierId;
    }

    public void setModifierId(Integer  modifierId) {
        this.modifierId = modifierId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}