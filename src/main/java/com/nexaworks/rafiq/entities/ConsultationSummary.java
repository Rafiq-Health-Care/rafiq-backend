package com.nexaworks.rafiq.entities;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"consultation", "doctor", "patient"})
@EqualsAndHashCode(callSuper = false, of = {"consultation"})
@Table(name = "consultation_summary", indexes = {
        @Index(name = "idx_consultation_summary_consultation", columnList = "consultation_id")})
public class ConsultationSummary extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String recoveryPlan;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<MedicineSummary> medicineSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> requiredLabTest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", referencedColumnName = "id", nullable = false, unique = true)
    private Consultation consultation;
}
