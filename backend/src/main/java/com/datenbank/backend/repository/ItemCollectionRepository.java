package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemCollectionRepository
        extends JpaRepository<ItemCollection, Integer> {

    List<ItemCollection> findByParentItemIsNull();

    // 1.3 prüfen ob ein Item eine Kollektion ist
    boolean existsByParentItem_ItemId(Integer itemId);
}