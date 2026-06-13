package com.datenbank.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_content_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemContentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_content_type_id")
    private Integer itemContentTypeId;

    @Column(name = "item_content_type_name", nullable = false, columnDefinition = "TEXT")
    private String itemContentTypeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
