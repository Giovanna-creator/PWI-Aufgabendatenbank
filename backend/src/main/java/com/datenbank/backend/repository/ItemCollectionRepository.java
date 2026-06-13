package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemCollectionRepository
        extends JpaRepository<ItemCollection, Integer> {

    Optional<ItemCollection> findByParentItem_ItemId(Integer parentItemId);

    boolean existsByParentItem_ItemId(Integer itemId);
}
