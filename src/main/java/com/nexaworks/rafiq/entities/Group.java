package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"medicines", "patient"})
@Entity
@Table(name = "groups", indexes = {@Index(name = "patient_idx", columnList = "patient_id"),
        @Index(name = "group_idx", columnList = "id")})
public class Group extends BaseEntity {
    private String name;
    private String description;
    private String iconId;
    private String color;
    @OneToMany(mappedBy = "group", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Medicine> medicines;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

}
