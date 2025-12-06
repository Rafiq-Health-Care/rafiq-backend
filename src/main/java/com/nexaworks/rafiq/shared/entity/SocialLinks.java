package com.nexaworks.rafiq.shared.entity;

import com.nexaworks.rafiq.doctor.entity.model.Doctor;
import com.nexaworks.rafiq.lab.entity.Lab;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
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

    @OneToOne(mappedBy = "socialLinks")
    private Lab lab;
}
