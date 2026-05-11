package com.nexaworks.rafiq.repository.specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import com.nexaworks.rafiq.dto.request.doctor.DoctorFilter;
import com.nexaworks.rafiq.entities.DoctorSearchView;

import jakarta.persistence.criteria.Predicate;
public class DoctorSpecification {
    public static Specification<DoctorSearchView> search(DoctorFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Optional.ofNullable(filter.gender())
                    .ifPresent(gender -> predicates.add(cb.equal(root.get("gender"), gender)));

            Optional.ofNullable(filter.minPrice()).ifPresent(minPrice -> predicates
                    .add(cb.greaterThanOrEqualTo(root.get("price"), minPrice)));

            Optional.ofNullable(filter.maxPrice()).ifPresent(
                    maxPrice -> predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice)));

            Optional.ofNullable(filter.specialities()).ifPresent(
                    specialities -> predicates.add(root.get("specialization").in(specialities)));

            Optional.ofNullable(filter.availability()).ifPresent(availability -> {
                LocalDateTime to = availability.getDateTime().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("nextAvailable"), to));
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
