package com.datenbank.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tag_id;

    @ManyToOne
    @JoinColumn(name = "parent_tag_id")
    private Tag parentTag; // selbstreferenzierend

    private String tag;
    private String description;

    // Getter & Setter
    public Long getTag_id() { return tag_id; }
    public void setTag_id(Long tag_id) { this.tag_id = tag_id; }

    public Tag getParentTag() { return parentTag; }
    public void setParentTag(Tag parentTag) { this.parentTag = parentTag; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}