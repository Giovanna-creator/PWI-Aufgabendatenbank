package com.datenbank.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "item_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_type_id")
    private Integer itemTypeId;

    @Column(name = "item_type_name", nullable = false, columnDefinition = "TEXT")
    private String itemTypeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_content_types",
            joinColumns = @JoinColumn(name = "item_type_id"),
            inverseJoinColumns = @JoinColumn(name = "item_content_type_id")
    )
    private Set<ItemContentType> allowedContentTypes = new HashSet<>();
}
