package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class TagDTO {

    private Long tag_id;

    @NotBlank(message = "Tag ist Pflicht")
    private String tag;

    private String description;
    private Long parent_tag_id; // nur die ID, nicht das ganze Objekt

    // Getter & Setter
    public Long getTag_id() { return tag_id; }
    public void setTag_id(Long tag_id) { this.tag_id = tag_id; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getParent_tag_id() { return parent_tag_id; }
    public void setParent_tag_id(Long parent_tag_id) { this.parent_tag_id = parent_tag_id; }
}