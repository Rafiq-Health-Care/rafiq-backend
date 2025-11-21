package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Company extends BaseEntity {
    String name;
    String country;
    String description;
    @ManyToMany(mappedBy = "companies")
    List<Drug> drugs;
}
