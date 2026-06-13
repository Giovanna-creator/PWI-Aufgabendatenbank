package com.datenbank.backend.dto;

public class FrontendCollectionItemDto {

    private String id;
    private String childItemId;
    private Integer position;
    private Boolean order;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getChildItemId() { return childItemId; }
    public void setChildItemId(String childItemId) { this.childItemId = childItemId; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getOrder() { return order; }
    public void setOrder(Boolean order) { this.order = order; }
}
