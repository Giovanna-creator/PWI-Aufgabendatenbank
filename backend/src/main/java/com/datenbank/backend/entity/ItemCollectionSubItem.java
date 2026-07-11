package com.datenbank.backend.entity;

import jakarta.persistence.*;

/** Join-Tabelle mit zusätzl. "position"-Attribut → eigene Entity + Composite Key nötig. */
@Entity
@Table(name = "item_collection_sub_item")
public class ItemCollectionSubItem {

    @EmbeddedId
    private ItemCollectionSubItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemCollectionId")
    @JoinColumn(name = "item_collection_id")
    private ItemCollection itemCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subitemId")
    @JoinColumn(name = "subitem_id")
    private Item subItem;

    @Column(name = "position")
    private Integer position;

    public ItemCollectionSubItem() {
    }

    public ItemCollectionSubItem(ItemCollection itemCollection, Item subItem, Integer position) {
        this.itemCollection = itemCollection;
        this.subItem = subItem;
        this.position = position;
        this.id = new ItemCollectionSubItemId(itemCollection.getItemCollectionId(), subItem.getItemId());
    }

    public ItemCollectionSubItemId getId() {
        return id;
    }

    public void setId(ItemCollectionSubItemId id) {
        this.id = id;
    }

    public ItemCollection getItemCollection() {
        return itemCollection;
    }

    public void setItemCollection(ItemCollection itemCollection) {
        this.itemCollection = itemCollection;
    }

    public Item getSubItem() {
        return subItem;
    }

    public void setSubItem(Item subItem) {
        this.subItem = subItem;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}