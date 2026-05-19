package com.nexaworks.rafiq.repository.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import com.nexaworks.rafiq.dto.response.consultation.ConsultationFilter;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.SlotStatus;

import jakarta.persistence.criteria.Predicate;

public class ConsultationSpecification {
    public static Specification<Consultation> filterConsultation(ConsultationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Optional.ofNullable(filter.doctorId()).ifPresent(
                    id -> predicates.add(cb.equal(root.get("slot").get("doctor").get("id"), id)));

            Optional.ofNullable(filter.specialization())
                    .ifPresent(specialization -> predicates.add(cb.equal(
                            root.get("slot").get("doctor").get("specialization"), specialization)));

            Optional.ofNullable(filter.fromStartTime()).ifPresent(fromStartTime -> predicates.add(
                    cb.greaterThanOrEqualTo(root.get("slot").get("startTime"), fromStartTime)));

            Optional.ofNullable(filter.toStartTime()).ifPresent(toStartTime -> predicates
                    .add(cb.lessThanOrEqualTo(root.get("slot").get("startTime"), toStartTime)));

            Optional.ofNullable(filter.fromPrice()).ifPresent(fromPrice -> predicates.add(cb
                    .greaterThanOrEqualTo(root.get("slot").get("doctor").get("price"), fromPrice)));

            Optional.ofNullable(filter.toPrice()).ifPresent(toPrice -> predicates.add(
                    cb.lessThanOrEqualTo(root.get("slot").get("doctor").get("price"), toPrice)));

            predicates.add(cb.equal(root.get("slot").get("status"), SlotStatus.AVAILABLE));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
