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
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class ActiveIngredient extends BaseEntity {
    String name;
    String description;
    @ManyToMany(mappedBy = "activeIngredients")
    List<Drug> drugs;

}
