package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entspricht der Tabelle "item_content_type".
 * Typen von Item-Inhalten (z.B. text/plain, application/json, image/png).
 */
@Entity
@Table(name = "item_content_type")
public class ItemContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_content_type_id")
    private UUID itemContentTypeId;

    @Column(name = "item_content_type_name", nullable = false, columnDefinition = "TEXT")
    private String itemContentTypeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public ItemContentType() {
    }

    public ItemContentType(String itemContentTypeName, String description) {
        this.itemContentTypeName = itemContentTypeName;
        this.description = description;
    }

    public UUID getItemContentTypeId() {
        return itemContentTypeId;
    }

    public void setItemContentTypeId(UUID itemContentTypeId) {
        this.itemContentTypeId = itemContentTypeId;
    }

    public String getItemContentTypeName() {
        return itemContentTypeName;
    }

    public void setItemContentTypeName(String itemContentTypeName) {
        this.itemContentTypeName = itemContentTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}