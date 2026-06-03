package com.datenbank.backend.repository;

import com.datenbank.backend.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository 
        extends JpaRepository<Item, Integer> {

    // 1.1 Filter root=true → Items ohne Parent
    List<Item> findByRootItemIsNull();

    // 1.2 Filter rootItemId=5 → Kinder eines Items
    List<Item> findByRootItem_ItemId(Integer rootItemId);
}