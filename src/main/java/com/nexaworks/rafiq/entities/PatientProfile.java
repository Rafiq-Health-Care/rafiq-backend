package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class PatientProfile  extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String description;

    @OneToOne(mappedBy = "patientProfile")
    private User user;
}
