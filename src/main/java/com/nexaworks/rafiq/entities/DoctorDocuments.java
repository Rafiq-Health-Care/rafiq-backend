package com.nexaworks.rafiq.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class DoctorDocuments extends BaseEntity{
    @Id
    private UUID id;
    private String nationalId;
    private String personalPhoto;
    private String medicalCertificate;
    private String hospitalId;
}
