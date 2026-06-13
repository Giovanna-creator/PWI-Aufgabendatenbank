package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, Integer> {
    Optional<ItemType> findByItemTypeName(String itemTypeName);
}
