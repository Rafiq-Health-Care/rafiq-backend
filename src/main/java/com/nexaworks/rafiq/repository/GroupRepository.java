package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Page<Group> findByPatientProfileId(UUID patientId, Pageable pageable);

    boolean existsGroupByName(String name);
}
