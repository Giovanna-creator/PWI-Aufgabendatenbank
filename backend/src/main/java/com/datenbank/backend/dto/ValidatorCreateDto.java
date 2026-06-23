package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidatorCreateDto {

    @NotBlank(message = "Beschreibung ist Pflicht")
    private String description;

    @NotBlank(message = "Regeltext ist Pflicht")
    private String validator;

    public ValidatorCreateDto() {
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
