package com.datenbank.backend.repository;

import com.datenbank.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    // Alle Tags die einen bestimmten Parent haben
    List<Tag> findByParentTag_TagId(Integer parentTagId);

    // Alle Root-Tags (ohne Parent)
    List<Tag> findByParentTagIsNull();
}