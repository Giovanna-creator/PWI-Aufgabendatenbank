package com.datenbank.backend.dto;

public class FrontendContentDto {

    private String id;
    private String purpose;
    private String jsonContent;
    private String blobContent;
    private String contentType;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getJsonContent() { return jsonContent; }
    public void setJsonContent(String jsonContent) { this.jsonContent = jsonContent; }
    public String getBlobContent() { return blobContent; }
    public void setBlobContent(String blobContent) { this.blobContent = blobContent; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
