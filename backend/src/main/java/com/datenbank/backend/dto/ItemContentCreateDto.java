package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO für das Erstellen und Aktualisieren eines ItemContents.
 *
 * Wird verwendet bei:
 *  - POST /api/item-contents
 *  - PUT  /api/item-contents/{id}
 *
 * Enthält KEINE itemContentId — diese wird automatisch generiert.
 */
public class ItemContentCreateDto {

    @NotNull(message = "License ist Pflicht")
    private UUID licenseId;

    @NotNull(message = "ItemContentType ist Pflicht")
    private UUID itemContentTypeId;

    @NotNull(message = "Author ist Pflicht")
    private UUID authorId;

    /**
     * Optional: JSON-Inhalt (z. B. Aufgabenstruktur).
     */
    private String jsonSerializedContent;

    /**
     * Optional: Binärdaten (PDF, Bild, etc.).
     */
    private byte[] blobSerializedContent;

    /**
     * Optional: Verknüpfte Tags.
     */
    private Set<UUID> tagIds = new HashSet<>();

    public ItemContentCreateDto() {
    }

    public UUID getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(UUID licenseId) {
        this.licenseId = licenseId;
    }

    public UUID getItemContentTypeId() {
        return itemContentTypeId;
    }

    public void setItemContentTypeId(UUID itemContentTypeId) {
        this.itemContentTypeId = itemContentTypeId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getJsonSerializedContent() {
        return jsonSerializedContent;
    }

    public void setJsonSerializedContent(String jsonSerializedContent) {
        this.jsonSerializedContent = jsonSerializedContent;
    }

    public byte[] getBlobSerializedContent() {
        return blobSerializedContent;
    }

    public void setBlobSerializedContent(byte[] blobSerializedContent) {
        this.blobSerializedContent = blobSerializedContent;
    }

    public Set<UUID> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<UUID> tagIds) {
        this.tagIds = tagIds;
    }
}

