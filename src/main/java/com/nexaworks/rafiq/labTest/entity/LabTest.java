package com.nexaworks.rafiq.labTest.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;

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
@Table(name = "lab_test", schema = "lab_test_schema")
public class LabTest extends BaseEntity {
    private String name;
    private String description;
    private String code;
    private UUID labId;
    private UUID fileId;
    private UUID doctorId;
    private UUID patientId;

    @OneToMany(mappedBy = "labTest", cascade = CascadeType.REMOVE)
    private List<LabResult> labResults;
    private Instant date;
}
