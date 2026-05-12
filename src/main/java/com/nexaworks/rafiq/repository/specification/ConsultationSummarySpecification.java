package com.nexaworks.rafiq.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.nexaworks.rafiq.dto.request.summary.ConsultationSummaryFilter;
import com.nexaworks.rafiq.entities.ConsultationSummary;

import jakarta.persistence.criteria.Predicate;

public class ConsultationSummarySpecification {

    public static Specification<ConsultationSummary> filter(ConsultationSummaryFilter filter,
            UUID patientId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("patient").get("id"), patientId));

            Optional.ofNullable(filter.specialization()).ifPresent(specialization -> predicates
                    .add(cb.equal(root.get("doctor").get("specialization"), specialization)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
