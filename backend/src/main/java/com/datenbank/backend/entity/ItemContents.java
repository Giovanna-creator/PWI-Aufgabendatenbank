package com.datenbank.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;
}
