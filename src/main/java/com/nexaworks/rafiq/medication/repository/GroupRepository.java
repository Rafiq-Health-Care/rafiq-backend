package com.nexaworks.rafiq.medication.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.medication.entity.model.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Page<Group> findByPatientId(UUID patientId, Pageable pageable);

    boolean existsGroupByPatientIdAndName(UUID patientId, String name);
}
