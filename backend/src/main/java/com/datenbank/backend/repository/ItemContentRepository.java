package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemContentRepository
        extends JpaRepository<ItemContent, Integer> {
}

