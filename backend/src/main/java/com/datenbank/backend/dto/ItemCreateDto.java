package com.datenbank.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

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
    private Integer authorId;

    @NotNull(message = "License ist Pflicht")
    private Integer licenseId;

    @NotNull(message = "ItemType ist Pflicht")
    private Integer itemTypeId;

    /**
     * Optional: Template zur Darstellung der Aufgabe.
     */
    private Integer itemTemplateId;

    /**
     * Optional: Verweis auf die Ursprungs-Aufgabe (für Varianten).
     */
    private Integer rootItemId;

    /**
     * Optional: IDs der Tags, die mit der Aufgabe verknüpft werden sollen.
     * Beim Erstellen leer lassen, falls keine Tags zugewiesen werden.
     */
    private Set<Integer> tagIds = new HashSet<>();

    /**
     * Optional: IDs der Validatoren für diese Aufgabe.
     */
    private Set<Integer> validatorIds = new HashSet<>();

    /**
     * Optional: IDs der Modifier (Varianten-Regeln) für diese Aufgabe.
     */
    private Set<Integer> modifierIds = new HashSet<>();

    public ItemCreateDto() {
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public Integer getItemTypeId() {
        return itemTypeId;
    }

    public void setItemTypeId(Integer itemTypeId) {
        this.itemTypeId = itemTypeId;
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
}