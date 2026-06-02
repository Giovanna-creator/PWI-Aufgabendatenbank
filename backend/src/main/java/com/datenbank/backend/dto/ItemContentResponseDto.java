package com.datenbank.backend.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO für die Rückgabe eines ItemContents an das Frontend.
 *
 * Wird verwendet bei:
 *  - GET /api/item-contents
 *  - GET /api/item-contents/{id}
 *  - POST /api/item-contents
 *  - PUT /api/item-contents/{id}
 */
public class ItemContentResponseDto {

    private Integer itemContentId;

    // License
    private Integer licenseId;
    private String licenseName;

    // ItemContentType
    private Integer itemContentTypeId;
    private String itemContentTypeName;

    // Author
    private Integer authorId;
    private String authorDescriptor;

    // Content
    private String jsonSerializedContent;

    /**
     * Gibt an ob Binärdaten vorhanden sind.
     * Der eigentliche Blob wird über separaten Endpoint geholt.
     */
    private boolean hasBlobContent;

    // Tags
    private Set<Integer> tagIds = new HashSet<>();

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemContentResponseDto() {
    }

    public Integer getItemContentId() {
        return itemContentId;
    }

    public void setItemContentId(Integer itemContentId) {
        this.itemContentId = itemContentId;
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
    }

    public Integer getItemContentTypeId() {
        return itemContentTypeId;
    }

    public void setItemContentTypeId(Integer itemContentTypeId) {
        this.itemContentTypeId = itemContentTypeId;
    }

    public String getItemContentTypeName() {
        return itemContentTypeName;
    }

    public void setItemContentTypeName(String itemContentTypeName) {
        this.itemContentTypeName = itemContentTypeName;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getAuthorDescriptor() {
        return authorDescriptor;
    }

    public void setAuthorDescriptor(String authorDescriptor) {
        this.authorDescriptor = authorDescriptor;
    }

    public String getJsonSerializedContent() {
        return jsonSerializedContent;
    }

    public void setJsonSerializedContent(String jsonSerializedContent) {
        this.jsonSerializedContent = jsonSerializedContent;
    }

    public boolean isHasBlobContent() { return hasBlobContent; }

    public void setHasBlobContent(boolean b) { this.hasBlobContent = b; }

    public Set<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<Integer> tagIds) {
        this.tagIds = tagIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

