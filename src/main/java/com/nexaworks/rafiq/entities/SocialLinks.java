package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(exclude = "doctor")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "social_links", indexes = {
        @Index(name = "idx_social_links_doctor", columnList = "doctor_id")})
public class SocialLinks extends BaseEntity {
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;
    private String youtube;
    private String whatsapp;
    private String website;

    @OneToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
}
