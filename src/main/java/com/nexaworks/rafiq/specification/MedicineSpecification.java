package com.nexaworks.rafiq.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.nexaworks.rafiq.dto.request.medicine.MedicineFilter;
import com.nexaworks.rafiq.entities.Medicine;

import jakarta.persistence.criteria.Predicate;

public class MedicineSpecification {
    public static Specification<Medicine> filter(MedicineFilter filter, UUID patientId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Optional.ofNullable(filter.status()).ifPresent(status -> {
                predicates.add(cb.equal(root.get("status"), status));
            });
            Optional.ofNullable(filter.type()).ifPresent(type -> {
                predicates.add(cb.equal(root.get("type"), type));
            });
            Optional.ofNullable(filter.groupId()).ifPresent(groupId -> {
                predicates.add(cb.equal(root.get("groupId"), groupId));
            });
            Optional.ofNullable(patientId).ifPresent(patientId1 -> {
                predicates.add(cb.equal(root.get("patient").get("id"), patientId1));
            });
            Optional.ofNullable(filter.search()).ifPresent(search -> {
                Predicate fullTextSearch = cb.isTrue(cb.function("ts_match", Boolean.class,
                        root.get("searchVector"), cb.literal(search)));

                Predicate partialSearch = cb.like(cb.lower(root.get("name")),
                        "%" + search.toLowerCase() + "%");

                Predicate fuzzySearch = cb.greaterThanOrEqualTo(cb.function("similarity",
                        Double.class, cb.lower(root.get("name")), cb.literal(search.toLowerCase())),
                        0.3);

                predicates.add(cb.or(fullTextSearch, partialSearch, fuzzySearch));
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}