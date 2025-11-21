package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nexaworks.rafiq.entities.Medicine;

public interface MedicineRepository
        extends
            JpaRepository<Medicine, UUID>,
            JpaSpecificationExecutor<Medicine> {
    boolean existsByPatientIdAndDrugId(UUID id, UUID drugId);

    int countByPatientId(UUID id);

}
