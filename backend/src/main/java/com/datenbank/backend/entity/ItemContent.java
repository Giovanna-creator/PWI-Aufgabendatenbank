package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entspricht der Tabelle "item_content".
 * Inhaltsbausteine der Aufgaben (Text, JSON, Binärdaten).
 *
 * Beziehungen:
 *  - @ManyToOne zu License, ItemContentType, Author (Level-1-Entitäten)
 *  - @ManyToMany zu Tag (über item_content_tags)
 */
@Entity
@Table(name = "item_content")
public class ItemContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_content_id")
    private UUID itemContentId;

    /**
     * FK auf license. @ManyToOne: viele Contents können dieselbe Lizenz haben.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_content_type_id", nullable = false)
    private ItemContentType itemContentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    /**
     * JSONB-Spalte. Wir mappen sie als String mit columnDefinition = "jsonb".
     * Hibernate liest/schreibt den JSON-Text; für die meisten Anwendungsfälle
     * ausreichend. Bei Bedarf später auf hibernate-types umstellbar.
     */
    

    @Column(name = "json_serialized_content", columnDefinition = "jsonb")
    private String jsonSerializedContent;

    /**
     * BYTEA-Spalte für Binärdaten (Bilder, PDFs).
     */
    
    @Column(name = "blob_serialized_content")
    private byte[] blobSerializedContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * @ManyToMany zu Tag über die Join-Tabelle "item_content_tags".
     * Keine zusätzlichen Attribute -> @ManyToMany ohne eigene Klasse.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_content_tags",
            joinColumns = @JoinColumn(name = "item_content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * Wird automatisch vor dem Speichern gesetzt.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public ItemContent() {
    }

    public UUID getItemContentId() {
        return itemContentId;
    }

    public void setItemContentId(UUID itemContentId) {
        this.itemContentId = itemContentId;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public ItemContentType getItemContentType() {
        return itemContentType;
    }

    public void setItemContentType(ItemContentType itemContentType) {
        this.itemContentType = itemContentType;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
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
}