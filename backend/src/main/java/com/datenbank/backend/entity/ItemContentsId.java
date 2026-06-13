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
public class ItemContentsId implements Serializable {

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_content_id")
    private Integer itemContentId;
}
