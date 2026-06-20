package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@ToString(exclude = "drugs")
@Table(name = "company")
public class Company extends BaseEntity {
    String name;
    String country;
    String description;
    @ManyToMany(mappedBy = "companies")
    List<Drug> drugs;
}
