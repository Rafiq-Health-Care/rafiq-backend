package com.nexaworks.rafiq.repository.specification;

import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.entities.Consultation;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ScheduleSpecification {
    public static Specification<Consultation> filter(ScheduleFilter filter, UUID doctorId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("doctor").get("id"), doctorId));

            Optional.ofNullable(filter.status()).ifPresent(status -> {
                predicates.add(cb.equal(root.get("status"), status));
            });

            Optional.ofNullable(filter.startDate()).ifPresent(startDate -> {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeSlot").get("date"), startDate));
            });

            Optional.ofNullable(filter.endDate()).ifPresent(endDate -> {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeSlot").get("date"), endDate));
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
