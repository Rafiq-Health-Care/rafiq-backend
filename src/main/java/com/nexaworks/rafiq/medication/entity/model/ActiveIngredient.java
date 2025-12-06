package com.nexaworks.rafiq.medication.entity.model;

import java.util.List;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.*;
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
