package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    void deleteByIdAndPatientId(UUID id, UUID patientId);
}
