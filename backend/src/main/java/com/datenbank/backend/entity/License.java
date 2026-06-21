package com.datenbank.backend.entity;

import jakarta.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "license")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "license_id")
    private UUID licenseId;

    @Column(name = "license", nullable = false, unique = true, columnDefinition = "TEXT")
    private String license;

    public License() {
    }

    public License(String license) {
        this.license = license;
    }

    public UUID getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(UUID licenseId) {
        this.licenseId = licenseId;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}