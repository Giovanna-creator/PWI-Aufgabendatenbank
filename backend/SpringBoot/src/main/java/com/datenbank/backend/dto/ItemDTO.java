package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;

public class ItemDTO {
    private Long item_id;

    @NotNull(message = "Author ist Pflicht")
    private Long author_id;

    @NotNull(message = "License ist Pflicht")
    private Long license_id;

    @NotNull(message = "ItemType ist Pflicht")
    private Long item_type_id;

    private Long item_template_id;
    private Long root_item_id;

    // Getter & Setter
    public Long getItem_id() { return item_id; }
    public void setItem_id(Long item_id) { this.item_id = item_id; }

    public Long getAuthor_id() { return author_id; }
    public void setAuthor_id(Long author_id) { this.author_id = author_id; }

    public Long getLicense_id() { return license_id; }
    public void setLicense_id(Long license_id) { this.license_id = license_id; }

    public Long getItem_type_id() { return item_type_id; }
    public void setItem_type_id(Long item_type_id) { this.item_type_id = item_type_id; }

    public Long getItem_template_id() { return item_template_id; }
    public void setItem_template_id(Long item_template_id) { this.item_template_id = item_template_id; }

    public Long getRoot_item_id() { return root_item_id; }
    public void setRoot_item_id(Long root_item_id) { this.root_item_id = root_item_id; }
}