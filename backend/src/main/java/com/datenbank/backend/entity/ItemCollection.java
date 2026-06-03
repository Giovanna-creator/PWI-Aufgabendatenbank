package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Entspricht der Tabelle "item_collection".
 * Sammlungen von Aufgaben (Lernpfade, Sequenzen).
 *
 * Hinweis: Die Spalte "order" ist ein reserviertes SQL-Wort,
 * daher in der SQL-Definition in Anführungszeichen. In JPA mappen
 * wir das Feld als "collectionOrder" auf die Spalte "order".
 *
 * collectionOrder ist ein Boolean:
 *  - true: Kollektion ist geordnet (SubItems haben Positionen 1, 2, 3...)
 *  - false: Kollektion ist ungeordnet (Positionen werden nicht angezeigt)
 */
@Entity
@Table(name = "item_collection")
public class ItemCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_collection_id")
    private Integer itemCollectionId;

    /**
     * Optionale Verbindung zu einem übergeordneten Item.
     * ON DELETE CASCADE im Schema.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_item_id")
    private Item parentItem;

    /**
     * "order" ist reserviert -> wir nutzen Anführungszeichen im Spaltennamen.
     * true = geordnet, false = ungeordnet.
     */
    @Column(name = "\"order\"", nullable = false)
    private Boolean collectionOrder = false;

    /**
     * Liste aller Aufgaben (Items) die zu dieser Kollektion gehören.
     * Geordnet nach Position (aufsteigend).
     * Wird automatisch mitgelöscht wenn die Kollektion gelöscht wird (CASCADE).
     */
    @OneToMany(mappedBy = "itemCollection",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    private List<ItemCollectionSubItem> subItems;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ItemCollection() {
    }

    public Integer getItemCollectionId() {
        return itemCollectionId;
    }

    public void setItemCollectionId(Integer itemCollectionId) {
        this.itemCollectionId = itemCollectionId;
    }

    public Item getParentItem() {
        return parentItem;
    }

    public void setParentItem(Item parentItem) {
        this.parentItem = parentItem;
    }

    public Boolean getCollectionOrder() {
        return collectionOrder;
    }

    public void setCollectionOrder(Boolean collectionOrder) {
        this.collectionOrder = collectionOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ItemCollectionSubItem> getSubItems() { return subItems; }

    public void setSubItems(List<ItemCollectionSubItem> s) { this.subItems = s; }
}