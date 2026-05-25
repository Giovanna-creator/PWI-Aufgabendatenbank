package com.datenbank.backend.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO für die Rückgabe einer Aufgabe (Item) an das Frontend.
 *
 * Wird vom Backend an das Frontend gesendet bei:
 *  - GET  /api/items
 *  - GET  /api/items/{id}
 *  - POST /api/items (Antwort nach Erstellung)
 *  - PUT  /api/items/{id} (Antwort nach Aktualisierung)
 *
 * Enthält die itemId und die generierten Timestamps.
 * Die verknüpften Entitäten (Author, License, etc.) werden mit ihrem
 * lesbaren Namen ausgegeben, nicht nur als ID — bequemer für das Frontend.
 */
public class ItemResponseDto {

    private Integer itemId;

    // Author
    private Integer authorId;
    private String authorDescriptor;

    // License
    private Integer licenseId;
    private String licenseName;

    // ItemType
    private Integer itemTypeId;
    private String itemTypeName;

    // Optional: Template
    private Integer itemTemplateId;

    // Optional: Ursprungs-Aufgabe
    private Integer rootItemId;

    // Verknüpfte Tags, Validatoren, Modifier (nur IDs für Einfachheit)
    private Set<Integer> tagIds = new HashSet<>();
    private Set<Integer> validatorIds = new HashSet<>();
    private Set<Integer> modifierIds = new HashSet<>();

    // Timestamps (vom System gesetzt)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemResponseDto() {
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
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

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public Integer getItemTemplateId() {
        return itemTemplateId;
    }

    public void setItemTemplateId(Integer itemTemplateId) {
        this.itemTemplateId = itemTemplateId;
    }

    public Integer getRootItemId() {
        return rootItemId;
    }

    public void setRootItemId(Integer rootItemId) {
        this.rootItemId = rootItemId;
    }

    public Set<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<Integer> tagIds) {
        this.tagIds = tagIds;
    }

    public Set<Integer> getValidatorIds() {
        return validatorIds;
    }

    public void setValidatorIds(Set<Integer> validatorIds) {
        this.validatorIds = validatorIds;
    }

    public Set<Integer> getModifierIds() {
        return modifierIds;
    }

    public void setModifierIds(Set<Integer> modifierIds) {
        this.modifierIds = modifierIds;
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