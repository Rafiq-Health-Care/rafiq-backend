package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class Lab extends BaseEntity{
    private String name;
    @OneToMany(mappedBy = "lab")
    private List<Address> addresses;
    @OneToMany(mappedBy = "lab")
    private List<LabTest>  tests;
    private String logo;
}
