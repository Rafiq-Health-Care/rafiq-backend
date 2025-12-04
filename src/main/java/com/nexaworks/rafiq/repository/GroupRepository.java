package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.entities.Patient;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Page<Group> findByPatientId(UUID patientId, Pageable pageable);

    boolean existsGroupByName(String name);

    boolean existsGroupByPatient_IdAndName(UUID patientProfileId, String name);

    boolean existsGroupByName_AndPatient(String name, Patient patient);
}
