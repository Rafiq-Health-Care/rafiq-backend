package com.nexaworks.rafiq.entities;

import java.util.List;

import com.nexaworks.rafiq.entities.enums.Color;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "groups")
public class Group extends BaseEntity {
    private String name;
    private String description;
    private String iconPublicId;
    private String iconUrl;
    @Enumerated(EnumType.STRING)
    private Color color;
    @OneToMany(mappedBy = "group", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Medicine> medicines;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

}
