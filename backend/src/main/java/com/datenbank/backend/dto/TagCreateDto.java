package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class TagCreateDto {

    @NotBlank(message = "Tag ist Pflicht")
    private String tag;

    private String description;

    private Integer parentTagId;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getParentTagId() {
        return parentTagId;
    }

    public void setParentTagId(Integer parentTagId) {
        this.parentTagId = parentTagId;
    }
}