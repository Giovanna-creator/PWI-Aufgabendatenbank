package com.datenbank.backend.entity;

import jakarta.persistence.*;

/**
 * Entspricht der Tabelle "item_representation_template".
 * Templates zur Darstellung der Aufgaben in der Oberfläche.
 */
@Entity
@Table(name = "item_representation_template")
public class ItemRepresentationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_template_id")
    private Integer itemTemplateId;

    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;

    public ItemRepresentationTemplate() {
    }

    public ItemRepresentationTemplate(String template) {
        this.template = template;
    }

    public Integer getItemTemplateId() {
        return itemTemplateId;
    }

    public void setItemTemplateId(Integer itemTemplateId) {
        this.itemTemplateId = itemTemplateId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}