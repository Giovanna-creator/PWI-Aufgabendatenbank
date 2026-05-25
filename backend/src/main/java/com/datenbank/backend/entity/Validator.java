package com.datenbank.backend.entity;

import jakarta.persistence.*;

/**
 * Entspricht der Tabelle "validator".
 * Validatoren für Aufgaben (z.B. "muss INNER JOIN enthalten").
 */
@Entity
@Table(name = "validator")
public class Validator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "validator_id")
    private Integer validatorId;

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

    public Integer getValidatorId() {
        return validatorId;
    }

    public void setValidatorId(Integer validatorId) {
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