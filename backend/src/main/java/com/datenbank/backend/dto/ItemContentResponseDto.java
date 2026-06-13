package com.datenbank.backend.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    private UUID itemContentId;

    // License
    private UUID licenseId;
    private String licenseName;

    // ItemContentType
    private UUID itemContentTypeId;
    private String itemContentTypeName;

    // Author
    private UUID authorId;
    private String authorDescriptor;

    // Content
    private String jsonSerializedContent;

    /**
     * Gibt an ob Binärdaten vorhanden sind.
     * Der eigentliche Blob wird über separaten Endpoint geholt.
     */
    private boolean hasBlobContent;

    // Tags
    private Set<UUID> tagIds = new HashSet<>();

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemContentResponseDto() {
    }

    public UUID getItemContentId() {
        return itemContentId;
    }

    public void setItemContentId(UUID itemContentId) {
        this.itemContentId = itemContentId;
    }

    public UUID getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(UUID licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicenseName() {
        return licenseName;
    }

    public void setLicenseName(String licenseName) {
        this.licenseName = licenseName;
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

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
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

    public Set<UUID> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<UUID> tagIds) {
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

