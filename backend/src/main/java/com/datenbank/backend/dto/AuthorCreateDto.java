package com.datenbank.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthorCreateDto {

    @NotBlank
    private String descriptor;

    @Email
    @NotBlank
    private String mail;

    // getters/setters
}