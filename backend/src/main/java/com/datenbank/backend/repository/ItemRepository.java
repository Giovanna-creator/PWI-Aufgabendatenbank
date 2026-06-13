package com.datenbank.backend.repository;

import com.datenbank.backend.entity.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByRootItemIsNull();

    List<Item> findByRootItem_ItemId(Integer rootItemId);

    @EntityGraph(attributePaths = {"author", "license", "itemType", "itemTemplate", "rootItem", "itemContents", "itemContents.itemContent", "itemContents.itemContent.itemContentType"})
    List<Item> findAll();

    @EntityGraph(attributePaths = {"author", "license", "itemType", "itemTemplate", "rootItem", "itemContents", "itemContents.itemContent", "itemContents.itemContent.itemContentType"})
    List<Item> findByRootItemIsNullWithDetails();

    @EntityGraph(attributePaths = {"author", "license", "itemType", "itemTemplate", "rootItem", "itemContents", "itemContents.itemContent", "itemContents.itemContent.itemContentType"})
    Optional<Item> findByIdWithDetails(Integer id);
}
