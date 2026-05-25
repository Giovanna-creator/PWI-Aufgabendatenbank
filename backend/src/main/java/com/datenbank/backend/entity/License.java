package com.datenbank.backend.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "license")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Integer licenseId;

    @Column(name = "license", nullable = false, unique = true, columnDefinition = "TEXT")
    private String license;

    public License() {
    }

    public License(String license) {
        this.license = license;
    }

    public Integer getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(Integer licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}