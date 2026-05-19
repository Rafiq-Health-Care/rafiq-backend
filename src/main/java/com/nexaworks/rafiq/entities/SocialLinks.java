package com.nexaworks.rafiq.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(exclude = "doctor")
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class SocialLinks extends BaseEntity {
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;
    private String youtube;
    private String whatsapp;
    private String website;

    @OneToOne(mappedBy = "socialLinks")
    private Doctor doctor;
}
