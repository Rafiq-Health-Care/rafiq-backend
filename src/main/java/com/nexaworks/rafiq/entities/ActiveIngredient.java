package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString(exclude = "drugs")
@Table(name = "active_ingredient")
@EqualsAndHashCode(callSuper = false, of = {"name"})
public class ActiveIngredient extends BaseEntity {
    String name;
    String description;
    @ManyToMany(mappedBy = "activeIngredients")
    List<Drug> drugs;

}
