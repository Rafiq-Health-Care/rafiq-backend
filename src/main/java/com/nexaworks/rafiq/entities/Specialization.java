package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class Specialization extends BaseEntity {

    private String name;
    private String description;
    private String code;

    @OneToMany(mappedBy = "specialization")
    private List<DoctorProfile> doctorProfiles;
}
