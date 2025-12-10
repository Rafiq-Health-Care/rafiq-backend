package com.nexaworks.rafiq.lab.entity;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import com.nexaworks.rafiq.shared.entity.SocialLinks;

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
@Table(name = "lab", schema = "lab_schema")
public class Lab extends BaseEntity {
    private String name;

    private String logoUrl;
    private String logoId;
    @Embedded
    private SocialLinks socialLinks;
}
