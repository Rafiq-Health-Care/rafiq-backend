package com.nexaworks.rafiq.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
public class Drug extends BaseEntity {
    String tradeName;
    String drugGroup;
    String dosageForm;
    String route;
    String pharmacology;
    double price;
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
