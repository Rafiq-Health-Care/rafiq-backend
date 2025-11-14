package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import java.util.List;
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
