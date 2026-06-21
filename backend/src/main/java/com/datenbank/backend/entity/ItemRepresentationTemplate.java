package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entspricht der Tabelle "item_representation_template".
 * Templates zur Darstellung der Aufgaben in der Oberfläche.
 */
@Entity
@Table(name = "item_representation_template")
public class ItemRepresentationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_template_id")
    private UUID itemTemplateId;

    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;

    public ItemRepresentationTemplate() {
    }

    public ItemRepresentationTemplate(String template) {
        this.template = template;
    }

    public UUID getItemTemplateId() {
        return itemTemplateId;
    }

    public void setItemTemplateId(UUID itemTemplateId) {
        this.itemTemplateId = itemTemplateId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}