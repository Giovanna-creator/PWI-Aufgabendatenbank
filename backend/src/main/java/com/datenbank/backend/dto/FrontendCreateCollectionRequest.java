package com.datenbank.backend.dto;

import java.util.List;

public class FrontendCreateCollectionRequest {

    private String itemType;
    private String author;
    private List<FrontendCreateContentRequest> contents;
    private Boolean order;

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public List<FrontendCreateContentRequest> getContents() { return contents; }
    public void setContents(List<FrontendCreateContentRequest> contents) { this.contents = contents; }
    public Boolean getOrder() { return order; }
    public void setOrder(Boolean order) { this.order = order; }
}
