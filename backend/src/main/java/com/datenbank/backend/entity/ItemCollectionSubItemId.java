package com.datenbank.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCollectionSubItemId implements Serializable {

    @Column(name = "item_collection_id")
    private Integer itemCollectionId;

    @Column(name = "subitem_id")
    private Integer subitemId;
}
