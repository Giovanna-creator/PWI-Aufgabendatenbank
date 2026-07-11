package com.datenbank.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.UUID;

/** DTO für die Rückgabe einer Aufgabe (Item) — mit lesbaren Namen der verknüpften Entitäten. */
public class ItemResponseDto {

    private UUID itemId;

    private UUID authorId;
    private String authorDescriptor;

    private UUID licenseId;
    private String licenseName;

    private UUID itemTypeId;
    private String itemTypeName;

    private UUID itemTemplateId;

    private UUID rootItemId;

    private Set<UUID> tagIds = new HashSet<>();
    private Set<UUID> validatorIds = new HashSet<>();
    private Set<UUID> modifierIds = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    

    private boolean isCollection;

    private Boolean order;

    private UUID collectionId;

    private List<ContentSummaryDto> contents = new ArrayList<>();

    // Getter & Setter
    @JsonProperty("isCollection")
    public boolean isCollection() { return isCollection; }
    public void setCollection(boolean c) { this.isCollection = c; }

    public Boolean getOrder() { return order; }
    public void setOrder(Boolean order) { this.order = order; }

    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }

    public List<ContentSummaryDto> getContents() { return contents; }
    public void setContents(List<ContentSummaryDto> c) { this.contents = c; }

    public ItemResponseDto() {
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
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

    public UUID getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(UUID itemTypeId) {
        this.itemTypeId = itemTypeId;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public UUID getItemTemplateId() {
        return itemTemplateId;
    }

    public void setItemTemplateId(UUID itemTemplateId) {
        this.itemTemplateId = itemTemplateId;
    }

    public UUID getRootItemId() {
        return rootItemId;
    }

    public void setRootItemId(UUID rootItemId) {
        this.rootItemId = rootItemId;
    }

    public Set<UUID> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<UUID> tagIds) {
        this.tagIds = tagIds;
    }

    public Set<UUID> getValidatorIds() {
        return validatorIds;
    }

    public void setValidatorIds(Set<UUID> validatorIds) {
        this.validatorIds = validatorIds;
    }

    public Set<UUID> getModifierIds() {
        return modifierIds;
    }

    public void setModifierIds(Set<UUID> modifierIds) {
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