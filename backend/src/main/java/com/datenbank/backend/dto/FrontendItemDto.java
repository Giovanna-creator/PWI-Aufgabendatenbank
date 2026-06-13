package com.datenbank.backend.dto;

import java.util.List;

public class FrontendItemDto {

    private String id;
    private String itemType;
    private String author;
    private String representationTemplate;
    private String license;
    private String rootItemId;
    private List<FrontendContentDto> contents;
    private List<FrontendCollectionItemDto> items;
    private Boolean order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getRepresentationTemplate() { return representationTemplate; }
    public void setRepresentationTemplate(String representationTemplate) { this.representationTemplate = representationTemplate; }
    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }
    public String getRootItemId() { return rootItemId; }
    public void setRootItemId(String rootItemId) { this.rootItemId = rootItemId; }
    public List<FrontendContentDto> getContents() { return contents; }
    public void setContents(List<FrontendContentDto> contents) { this.contents = contents; }
    public List<FrontendCollectionItemDto> getItems() { return items; }
    public void setItems(List<FrontendCollectionItemDto> items) { this.items = items; }
    public Boolean getOrder() { return order; }
    public void setOrder(Boolean order) { this.order = order; }
}
