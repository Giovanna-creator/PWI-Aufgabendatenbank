package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Beziehung: @ManyToMany zu ItemContentType über item_content_types
 * (definiert, welche Content-Typen für welchen Item-Typ erlaubt sind).
 */
@Entity
@Table(name = "item_type")
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_type_id")
    private UUID itemTypeId;

    @Column(name = "item_type_name", nullable = false, columnDefinition = "TEXT")
    private String itemTypeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_content_types",
            joinColumns = @JoinColumn(name = "item_type_id"),
            inverseJoinColumns = @JoinColumn(name = "item_content_type_id")
    )
    private Set<ItemContentType> allowedContentTypes = new HashSet<>();

    public ItemType() {
    }

    public ItemType(String itemTypeName, String description) {
        this.itemTypeName = itemTypeName;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ItemContentType> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(Set<ItemContentType> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}