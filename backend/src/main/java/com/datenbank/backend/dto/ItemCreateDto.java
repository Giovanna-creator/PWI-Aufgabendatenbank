package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DTO für das Erstellen und Aktualisieren einer Aufgabe (Item).
 *
 * Wird vom Frontend an das Backend gesendet bei:
 *  - POST /api/items (Erstellung)
 *  - PUT  /api/items/{id} (Aktualisierung)
 *
 * Enthält KEINE itemId — diese wird vom System automatisch vergeben
 * (bzw. kommt bei PUT aus der URL).
 */
public class ItemCreateDto {

    @NotNull(message = "Author ist Pflicht")
    private UUID authorId;

    @NotNull(message = "License ist Pflicht")
    private UUID licenseId;

    @NotNull(message = "ItemType ist Pflicht")
    private UUID itemTypeId;

    /**
     * Optional: Template zur Darstellung der Aufgabe.
     */
    private UUID itemTemplateId;

    /**
     * Optional: Verweis auf die Ursprungs-Aufgabe (für Varianten).
     */
    private UUID rootItemId;

    /**
     * Optional: IDs der Tags, die mit der Aufgabe verknüpft werden sollen.
     * Beim Erstellen leer lassen, falls keine Tags zugewiesen werden.
     */
    private Set<UUID> tagIds = new HashSet<>();

    /**
     * Optional: IDs der Validatoren für diese Aufgabe.
     */
    private Set<UUID> validatorIds = new HashSet<>();

    /**
     * Optional: IDs der Modifier (Varianten-Regeln) für diese Aufgabe.
     */
    private Set<UUID> modifierIds = new HashSet<>();

    public ItemCreateDto() {
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public UUID getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(UUID licenseId) {
        this.licenseId = licenseId;
    }

    public UUID getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(UUID itemTypeId) {
        this.itemTypeId = itemTypeId;
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
}