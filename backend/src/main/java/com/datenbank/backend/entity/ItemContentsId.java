package com.datenbank.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Zusammengesetzter Primärschlüssel für die Tabelle "item_contents".
 * Besteht aus item_id + item_content_id.
 */
@Embeddable
public class ItemContentsId implements Serializable {

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_content_id")
    private Integer itemContentId;

    public ItemContentsId() {
    }

    public ItemContentsId(Integer itemId, Integer itemContentId) {
        this.itemId = itemId;
        this.itemContentId = itemContentId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getItemContentId() {
        return itemContentId;
    }

    public void setItemContentId(Integer itemContentId) {
        this.itemContentId = itemContentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemContentsId that = (ItemContentsId) o;
        return Objects.equals(itemId, that.itemId)
                && Objects.equals(itemContentId, that.itemContentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemContentId);
    }
}