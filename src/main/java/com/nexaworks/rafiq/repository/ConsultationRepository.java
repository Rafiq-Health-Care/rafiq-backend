package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

}
