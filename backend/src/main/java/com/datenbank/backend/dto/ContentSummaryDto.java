package com.datenbank.backend.dto;

import java.util.UUID;

/**
 * Kompakte Darstellung eines ItemContent für die Item-Antwort.
 * Wird in ItemResponseDto als Liste verwendet.
 */
public class ContentSummaryDto {

    private UUID itemContentId;
    private String itemContentTypeName;
    private boolean hasJsonContent;
    private boolean hasBlobContent;

    public ContentSummaryDto() {}

    public UUID getItemContentId() { return itemContentId; }
    public void setItemContentId(UUID id) { this.itemContentId = id; }

    public String getItemContentTypeName() { return itemContentTypeName; }
    public void setItemContentTypeName(String n) { this.itemContentTypeName = n; }

    public boolean isHasJsonContent() { return hasJsonContent; }
    public void setHasJsonContent(boolean b) { this.hasJsonContent = b; }

    public boolean isHasBlobContent() { return hasBlobContent; }
    public void setHasBlobContent(boolean b) { this.hasBlobContent = b; }
}