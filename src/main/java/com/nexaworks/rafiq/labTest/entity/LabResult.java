package com.nexaworks.rafiq.labTest.entity;

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
@Table(name = "lab_result", schema = "lab_test_schema")
public class LabResult extends BaseEntity {

    private String name;
    private double result;
    private String unit;
    private String status;
    private String description;
    private String normalResult;

    @ManyToOne
    @JoinColumn(name = "lab_test_id")
    private LabTest labTest;
}
