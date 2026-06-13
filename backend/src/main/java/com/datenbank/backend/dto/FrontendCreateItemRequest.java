package com.datenbank.backend.dto;

import java.util.List;

public class FrontendCreateItemRequest {

    private String itemType;
    private String author;
    private String rootItemId;
    private List<FrontendCreateContentRequest> contents;

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getRootItemId() { return rootItemId; }
    public void setRootItemId(String rootItemId) { this.rootItemId = rootItemId; }
    public List<FrontendCreateContentRequest> getContents() { return contents; }
    public void setContents(List<FrontendCreateContentRequest> contents) { this.contents = contents; }
}
