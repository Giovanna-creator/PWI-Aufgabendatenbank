package com.datenbank.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_collection_sub_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCollectionSubItem {

    public ItemCollectionSubItem(ItemCollection itemCollection, Item subItem, Integer position) {
        this.itemCollection = itemCollection;
        this.subItem = subItem;
        this.position = position;
        this.id = new ItemCollectionSubItemId(
                itemCollection.getItemCollectionId(),
                subItem.getItemId()
        );
    }

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
}
