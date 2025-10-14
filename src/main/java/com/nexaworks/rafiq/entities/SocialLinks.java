package com.nexaworks.rafiq.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@Entity
public abstract class SocialLinks extends BaseEntity{
    private String facebook;
    private String twitter;
    private String instagram;
    private String linkedin;
    private String youtube;
    private String whatsapp;
    private String website;
    @OneToOne
    private DoctorProfile doctorProfile;
    @OneToOne
    private Lab lab;
}
