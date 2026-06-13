package com.datenbank.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_representation_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRepresentationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_template_id")
    private Integer itemTemplateId;

    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;
}
