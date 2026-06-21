package com.datenbank.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.UUID;

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

    private UUID itemId;

    // Author
    private UUID authorId;
    private String authorDescriptor;

    // License
    private UUID licenseId;
    private String licenseName;

    // ItemType
    private UUID itemTypeId;
    private String itemTypeName;

    // Optional: Template
    private UUID itemTemplateId;

    // Optional: Ursprungs-Aufgabe
    private UUID rootItemId;

    // Verknüpfte Tags, Validatoren, Modifier (nur IDs für Einfachheit)
    private Set<UUID> tagIds = new HashSet<>();
    private Set<UUID> validatorIds = new HashSet<>();
    private Set<UUID> modifierIds = new HashSet<>();

    // Timestamps (vom System gesetzt)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    

    /**
     * true wenn dieses Item eine Kollektion ist
     * (d.h. eine ItemCollection mit parent_item_id = item_id existiert)
     */
    private boolean isCollection;

    /**
     * Reihenfolge-Flag, falls dieses Item eine Kollektion ist.
     * true = geordnet, false = ungeordnet, null = keine Kollektion.
     * Damit das Frontend den Order-Status auch beim Laden über /items kennt.
     */
    private Boolean order;

    /**
     * Die item_collection_id, falls dieses Item eine Kollektion ist (sonst null).
     * Das Frontend braucht sie für alle /api/collections/{id}/... Aufrufe,
     * da diese Endpunkte über die Collection-ID adressiert werden, nicht über
     * die item_id.
     */
    private UUID collectionId;

    /**
     * Inhalte dieses Items. Niemals null — leere Liste wenn keine vorhanden.
     */
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