package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemCollectionRepository
        extends JpaRepository<ItemCollection, UUID> {

    List<ItemCollection> findByParentItemIsNull();

    // 1.3 prüfen ob ein Item eine Kollektion ist
    boolean existsByParentItem_ItemId(UUID itemId);

    // Die (erste) Kollektion zu einem Eltern-Item — für isCollection + order-Flag
    Optional<ItemCollection> findFirstByParentItem_ItemId(UUID itemId);
}