package com.datenbank.backend.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "item_content")
public class ItemContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_content_id")
    private UUID itemContentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_content_type_id", nullable = false)
    private ItemContentType itemContentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "json_serialized_content", columnDefinition = "jsonb")
    private String jsonSerializedContent;

    @Column(name = "blob_serialized_content")
    private byte[] blobSerializedContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_content_tags",
            joinColumns = @JoinColumn(name = "item_content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

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