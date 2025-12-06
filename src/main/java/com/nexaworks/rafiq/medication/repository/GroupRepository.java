package com.nexaworks.rafiq.medication.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.patient.entity.model.Patient;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Page<Group> findByPatientId(UUID patientId, Pageable pageable);

    boolean existsGroupByName(String name);

    boolean existsGroupByPatient_IdAndName(UUID patientProfileId, String name);

    boolean existsGroupByName_AndPatient(String name, Patient patient);
}
