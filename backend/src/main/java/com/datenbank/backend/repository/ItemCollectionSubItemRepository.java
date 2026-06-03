package com.datenbank.backend.repository;
import com.datenbank.backend.entity.ItemCollectionSubItem;
import com.datenbank.backend.entity.ItemCollectionSubItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemCollectionSubItemRepository extends JpaRepository<ItemCollectionSubItem, ItemCollectionSubItemId> {

    /**
     * Alle SubItems einer Kollektion sortiert nach Position.
     */
    List<ItemCollectionSubItem> 
        findByCollection_ItemCollectionIdOrderByPositionAsc(
            Integer collectionId);
}
