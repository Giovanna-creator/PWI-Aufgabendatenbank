package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entspricht der Tabelle "item" - die zentrale Aufgaben-Entität.
 *
 * Beziehungen:
 *  - @ManyToOne zu Author, License, ItemType, ItemRepresentationTemplate
 *  - Self-Reference: root_item_id (für Varianten)
 *  - @ManyToMany zu Tag (über item_tags)
 *  - @ManyToMany zu Validator (über item_validator)
 *  - @ManyToMany zu Modifier (über item_modifier)
 */
@Entity
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id")
    private UUID itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_type_id", nullable = false)
    private ItemType itemType;

    /**
     * Optionales Template (kann null sein -> ON DELETE SET NULL im Schema).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_template_id")
    private ItemRepresentationTemplate itemTemplate;

    /**
     * Self-Reference: Varianten verweisen auf ihre Ursprungs-Aufgabe.
     * @ManyToOne weil viele Varianten dasselbe Original haben können.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_item_id")
    private Item rootItem;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * @ManyToMany zu Tag über die Join-Tabelle "item_tags".
     * Diese Join-Tabelle hat KEINE zusätzlichen Attribute,
     * daher reicht @ManyToMany ohne eigene Entity-Klasse.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_tags",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_validator",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "validator_id")
    )
    private Set<Validator> validators = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_modifier",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "modifier_id")
    )
    private Set<Modifier> modifiers = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Item() {
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemRepresentationTemplate getItemTemplate() {
        return itemTemplate;
    }

    public void setItemTemplate(ItemRepresentationTemplate itemTemplate) {
        this.itemTemplate = itemTemplate;
    }

    public Item getRootItem() {
        return rootItem;
    }

    public void setRootItem(Item rootItem) {
        this.rootItem = rootItem;
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

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Validator> getValidators() {
        return validators;
    }

    public void setValidators(Set<Validator> validators) {
        this.validators = validators;
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

   
}