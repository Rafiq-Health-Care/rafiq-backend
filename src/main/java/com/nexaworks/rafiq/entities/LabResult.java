package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class LabResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private double result;
    private String unit;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String description;
    private String normalResult;
//    @ManyToOne
//    @JoinColumn(name = "lab_test_id")
//    private LabTest labTest;

}
