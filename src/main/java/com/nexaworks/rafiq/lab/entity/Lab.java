package com.nexaworks.rafiq.lab.entity;

import java.util.List;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.shared.entity.SocialLinks;
import com.nexaworks.rafiq.shared.entity.Address;

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
public class Lab extends BaseEntity {
    private String name;

    @OneToMany(mappedBy = "lab", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Address> addresses;

    @OneToMany(mappedBy = "lab")
    private List<LabTest> tests;

    private String logo;
    private String publicId;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "social_links_id", referencedColumnName = "id")
    private SocialLinks socialLinks;
}
