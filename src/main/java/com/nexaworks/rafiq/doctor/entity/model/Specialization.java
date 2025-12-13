package com.nexaworks.rafiq.doctor.entity.model;

import java.util.List;

import com.nexaworks.rafiq.shared.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "specialization", schema = "doctor_schema")
public class Specialization extends BaseEntity {

    private String name;
    private String description;
    private String code;

    @OneToMany(mappedBy = "specialization")
    private List<Doctor> doctors;
}
