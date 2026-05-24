package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(exclude = {"activeIngredients", "companies"})
@Entity

public class Drug extends BaseEntity {
    String tradeName;
    String drugGroup;
    String dosageForm;
    String route;
    @Column(columnDefinition = "TEXT")
    String pharmacology;
    double price;

    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

    @ManyToMany
    @JoinTable(name = "drug_active_ingredient", joinColumns = @JoinColumn(name = "drug_id"), inverseJoinColumns = @JoinColumn(name = "active_ingredient_id"))
    List<ActiveIngredient> activeIngredients;
    @ManyToMany
    @JoinTable(name = "drug_company", joinColumns = @JoinColumn(name = "drug_id"), inverseJoinColumns = @JoinColumn(name = "company_id"))
    List<Company> companies;
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Drug other = (Drug) obj;
        if (activeIngredients.size() != other.activeIngredients.size()) {
            return false;
        }
        for (ActiveIngredient ai : activeIngredients) {
            if (!other.activeIngredients.contains(ai)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
