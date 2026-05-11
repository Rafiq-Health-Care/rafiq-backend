package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nexaworks.rafiq.entities.DoctorSearchView;

public interface DoctorSearchViewRepository
        extends
            JpaRepository<DoctorSearchView, UUID>,
            JpaSpecificationExecutor<DoctorSearchView> {
}
