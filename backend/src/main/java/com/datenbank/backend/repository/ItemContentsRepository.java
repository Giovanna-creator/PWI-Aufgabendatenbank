package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemContents;
import com.datenbank.backend.entity.ItemContentsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;


@Repository
public interface ItemContentsRepository
        extends JpaRepository<ItemContents, ItemContentsId> {

    // Alle Contents eines Items laden
    List<ItemContents> findByItem_ItemId(UUID itemId);

    // Alle Verknüpfungen eines Contents laden (für purpose-Update)
    List<ItemContents> findByItemContent_ItemContentId(UUID itemContentId);
}