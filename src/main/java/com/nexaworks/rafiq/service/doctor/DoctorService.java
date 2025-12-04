package com.nexaworks.rafiq.service.doctor;

import java.util.UUID;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.Doctor;

public interface DoctorService {


    void updateNationalId(UploadResults uploadResults, UUID uuid);

    void register(Doctor user, UUID specialization,String description);
}
