package com.datenbank.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "validator")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Validator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "validator_id")
    private Integer validatorId;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "validator", nullable = false, columnDefinition = "TEXT")
    private String validator;
}
