package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemContents;
import com.datenbank.backend.entity.ItemContentsId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemContentsRepository extends JpaRepository<ItemContents, ItemContentsId> {

    List<ItemContents> findByItem_ItemId(Integer itemId);

    @EntityGraph(attributePaths = {"itemContent", "itemContent.itemContentType", "itemContent.author", "itemContent.license"})
    List<ItemContents> findByItem_ItemIdWithDetails(Integer itemId);

    @EntityGraph(attributePaths = {"itemContent", "itemContent.itemContentType", "itemContent.author", "itemContent.license"})
    Optional<ItemContents> findByItem_ItemIdAndItemContent_ItemContentId(Integer itemId, Integer contentId);
}
