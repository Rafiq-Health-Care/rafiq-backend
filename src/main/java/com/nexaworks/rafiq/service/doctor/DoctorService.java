package com.nexaworks.rafiq.service.doctor;

import java.util.UUID;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.enums.Specialization;

public interface DoctorService {

    void updateNationalId(UploadResults uploadResults, UUID uuid);

    void register(Doctor user, Specialization specialization, String description);
}
