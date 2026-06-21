package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entspricht der Tabelle "validator".
 * Validatoren für Aufgaben (z.B. "muss INNER JOIN enthalten").
 */
@Entity
@Table(name = "validator")
public class Validator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "validator_id")
    private UUID validatorId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "validator", nullable = false, columnDefinition = "TEXT")
    private String validator;

    public Validator() {
    }

    public Validator(String description, String validator) {
        this.description = description;
        this.validator = validator;
    }

    public UUID getValidatorId() {
        return validatorId;
    }

    public void setValidatorId(UUID validatorId) {
        this.validatorId = validatorId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }
}