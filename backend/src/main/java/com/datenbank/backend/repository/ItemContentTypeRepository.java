package com.datenbank.backend.repository;

import com.datenbank.backend.entity.ItemContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ItemContentTypeRepository extends JpaRepository<ItemContentType, UUID> {
}

