package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemCollectionSubItem;
import com.datenbank.backend.entity.ItemCollectionSubItemId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCollectionSubItemRepository extends JpaRepository<ItemCollectionSubItem, ItemCollectionSubItemId> {

    List<ItemCollectionSubItem> findByCollection_ItemCollectionIdOrderByPositionAsc(Integer collectionId);

    @EntityGraph(attributePaths = {"subItem", "subItem.author", "subItem.license", "subItem.itemType", "subItem.itemTemplate", "itemCollection"})
    List<ItemCollectionSubItem> findByCollection_ItemCollectionIdWithDetails(Integer collectionId);

    Optional<ItemCollectionSubItem> findByCollection_ItemCollectionIdAndSubItem_ItemId(Integer collectionId, Integer subItemId);

    void deleteByCollection_ItemCollectionIdAndSubItem_ItemId(Integer collectionId, Integer subItemId);
}
