package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

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
    private Integer licenseId;

    @NotNull(message = "ItemContentType ist Pflicht")
    private Integer itemContentTypeId;

    @NotNull(message = "Author ist Pflicht")
    private Integer authorId;

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
    private Set<Integer> tagIds = new HashSet<>();

    public ItemContentCreateDto() {
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public Integer getItemContentTypeId() {
        return itemContentTypeId;
    }

    public void setItemContentTypeId(Integer itemContentTypeId) {
        this.itemContentTypeId = itemContentTypeId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
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

    public Set<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<Integer> tagIds) {
        this.tagIds = tagIds;
    }
}

