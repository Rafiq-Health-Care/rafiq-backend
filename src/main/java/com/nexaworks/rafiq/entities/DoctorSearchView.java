package com.nexaworks.rafiq.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "doctor_search_view")
public class DoctorSearchView {

    @Id
    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "personal_photo")
    private String personalPhoto;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String specialization;
    private BigDecimal price;
    private BigDecimal rating;

    @Column(name = "experience_years")
    private int experienceYears;

    private String gender;

    @Column(name = "next_available")
    private LocalDateTime nextAvailable;
}
