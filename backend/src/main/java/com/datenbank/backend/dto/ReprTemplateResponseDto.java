package com.datenbank.backend.dto;

import java.util.UUID;

public class ReprTemplateResponseDto {

    private UUID id;
    private String template;

    public ReprTemplateResponseDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
