package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotEmpty;

public class ReprTemplateCreateDto {

    @NotEmpty(message = "Template-XML ist Pflicht")
    private String template;

    public ReprTemplateCreateDto() {
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
