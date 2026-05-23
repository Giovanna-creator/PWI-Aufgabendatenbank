package com.datenbank.backend.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer authorId;

    @Column(name = "descriptor", nullable = false)
    private String descriptor;

    @Column(name = "mail", columnDefinition = "TEXT" )
    private String mail;

    public Author() {
    }

    public Author(String descriptor, String mail) {
        this.descriptor = descriptor;
        this.mail = mail;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}