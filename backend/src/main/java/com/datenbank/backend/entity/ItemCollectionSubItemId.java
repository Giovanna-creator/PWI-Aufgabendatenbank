package com.datenbank.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Zusammengesetzter Primärschlüssel (Composite Key) für
 * die Tabelle "item_collection_sub_item".
 *
 * Eine @Embeddable-Klasse repräsentiert einen Schlüssel,
 * der aus mehreren Spalten besteht (hier: item_collection_id + subitem_id).
 *
 * Muss Serializable sein und equals()/hashCode() implementieren -
 * das ist eine JPA-Anforderung für Composite Keys.
 */
@Embeddable
public class ItemCollectionSubItemId implements Serializable {

    @Column(name = "item_collection_id")
    private Integer itemCollectionId;

    @Column(name = "subitem_id")
    private Integer subitemId;

    public ItemCollectionSubItemId() {
    }

    public ItemCollectionSubItemId(Integer itemCollectionId, Integer subitemId) {
        this.itemCollectionId = itemCollectionId;
        this.subitemId = subitemId;
    }

    public Integer getItemCollectionId() {
        return itemCollectionId;
    }

    public void setItemCollectionId(Integer itemCollectionId) {
        this.itemCollectionId = itemCollectionId;
    }

    public Integer getSubitemId() {
        return subitemId;
    }

    public void setSubitemId(Integer subitemId) {
        this.subitemId = subitemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCollectionSubItemId that = (ItemCollectionSubItemId) o;
        return Objects.equals(itemCollectionId, that.itemCollectionId)
                && Objects.equals(subitemId, that.subitemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemCollectionId, subitemId);
    }
}