package com.datenbank.backend.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Integer tagId;

    /**
     * Self-Reference: ein Tag kann ein Eltern-Tag haben.
     * @ManyToOne weil viele Tags dasselbe Eltern-Tag haben können.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tag_id")
    private Tag parentTag;

    @Column(name = "tag", nullable = false, columnDefinition = "TEXT")
    private String tag;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Tag() {
    }

    public Tag(String tag, String description, Tag parentTag) {
        this.tag = tag;
        this.description = description;
        this.parentTag = parentTag;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Tag getParentTag() {
        return parentTag;
    }

    public void setParentTag(Tag parentTag) {
        this.parentTag = parentTag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}