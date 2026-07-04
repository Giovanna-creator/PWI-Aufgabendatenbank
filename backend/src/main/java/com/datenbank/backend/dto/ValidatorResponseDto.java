package com.datenbank.backend.dto;

import java.util.UUID;

public class ValidatorResponseDto {

    private UUID validatorId;
    private String description;
    private String validator;

    public ValidatorResponseDto() {
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
