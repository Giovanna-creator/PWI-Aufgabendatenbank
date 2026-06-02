package com.datenbank.backend.dto;

/**
 * Kompakte Darstellung eines ItemContent für die Item-Antwort.
 * Wird in ItemResponseDto als Liste verwendet.
 */
public class ContentSummaryDto {

    private Integer itemContentId;
    private String itemContentTypeName;
    private boolean hasJsonContent;
    private boolean hasBlobContent;

    public ContentSummaryDto() {}

    public Integer getItemContentId() { return itemContentId; }
    public void setItemContentId(Integer id) { this.itemContentId = id; }

    public String getItemContentTypeName() { return itemContentTypeName; }
    public void setItemContentTypeName(String n) { this.itemContentTypeName = n; }

    public boolean isHasJsonContent() { return hasJsonContent; }
    public void setHasJsonContent(boolean b) { this.hasJsonContent = b; }

    public boolean isHasBlobContent() { return hasBlobContent; }
    public void setHasBlobContent(boolean b) { this.hasBlobContent = b; }
}