package com.datenbank.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Zusammengesetzter Primärschlüssel für die Tabelle "item_contents".
 * Besteht aus item_id + item_content_id.
 */
@Embeddable
public class ItemContentsId implements Serializable {

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "item_content_id")
    private UUID itemContentId;

    public ItemContentsId() {
    }

    public ItemContentsId(UUID itemId, UUID itemContentId) {
        this.itemId = itemId;
        this.itemContentId = itemContentId;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getItemContentId() {
        return itemContentId;
    }

    public void setItemContentId(UUID itemContentId) {
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