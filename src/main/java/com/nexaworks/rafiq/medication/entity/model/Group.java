package com.nexaworks.rafiq.medication.entity.model;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import com.nexaworks.rafiq.medication.entity.enums.Color;

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

    private UUID patientId;

}
