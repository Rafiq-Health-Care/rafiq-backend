package com.nexaworks.rafiq.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class Specialization  extends BaseEntity{
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String description;
    private String code;

    @OneToMany(mappedBy = "specialization")
    private List<DoctorProfile> doctorProfiles;

}
