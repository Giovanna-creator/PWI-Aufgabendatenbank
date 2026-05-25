package com.datenbank.backend.entity;

import jakarta.persistence.*;

/**
 * Entspricht der Tabelle "item_contents".
 *
 * Verknüpft Item mit ItemContent und hat ein zusätzliches
 * Attribut "purpose" (z.B. "Aufgabenstellung", "Hinweis", "Lösung").
 * Daher eigene Entity-Klasse mit Composite Key.
 */
@Entity
@Table(name = "item_contents")
public class ItemContents {

    @EmbeddedId
    private ItemContentsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemContentId")
    @JoinColumn(name = "item_content_id")
    private ItemContent itemContent;

    @Column(name = "purpose",columnDefinition = "TEXT")
    private String purpose;

    public ItemContents() {
    }

    public ItemContents(Item item, ItemContent itemContent, String purpose) {
        this.item = item;
        this.itemContent = itemContent;
        this.purpose = purpose;
        this.id = new ItemContentsId(item.getItemId(), itemContent.getItemContentId());
    }

    public ItemContentsId getId() {
        return id;
    }

    public void setId(ItemContentsId id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public ItemContent getItemContent() {
        return itemContent;
    }

    public void setItemContent(ItemContent itemContent) {
        this.itemContent = itemContent;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}