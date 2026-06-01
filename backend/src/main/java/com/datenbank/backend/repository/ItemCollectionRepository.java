package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemCollectionRepository
        extends JpaRepository<ItemCollection, Integer> {

    // Alle Root-Kollektionen (ohne Parent)
    List<ItemCollection> findByParentCollectionIsNull();

    // Alle Kinder einer Kollektion
    List<ItemCollection> findByParentCollection_ItemCollectionId(Integer parentId);
}