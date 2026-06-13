package com.datenbank.backend.dto;

import java.util.UUID;

public class AddItemToCollectionDto {

    private UUID itemId;

    public AddItemToCollectionDto() {}

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
}