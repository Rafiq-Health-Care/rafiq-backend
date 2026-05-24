package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"labResults", "patient", "doctor"})
@Entity
@Table(name = "lab_test")
public class LabTest extends BaseEntity {

    private String name;
    private String description;
    private String code;
    // todo refactor this to extract only don't save pdf
    private String pdf;
    private String publicId;
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = true)
    private Patient patient;

    @OneToMany(mappedBy = "labTest", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST})
    private List<LabResult> labResults;

    private LocalDateTime date;
}
