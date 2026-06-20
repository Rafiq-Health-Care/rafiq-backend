package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"labTest"})
@EqualsAndHashCode(callSuper = false, of = {"name"})
@Entity
@Table(name = "lab_result", indexes = {
        @Index(name = "idx_lab_result_lab_test", columnList = "lab_test_id")})
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
