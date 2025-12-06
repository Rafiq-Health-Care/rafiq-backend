package com.nexaworks.rafiq.doctor.service;

import java.util.UUID;

import com.nexaworks.rafiq.fileManagment.api.dto.UploadResults;
import com.nexaworks.rafiq.doctor.entity.model.Doctor;

public interface DoctorService {


    void updateNationalId(UploadResults uploadResults, UUID uuid);

    void register(Doctor user, UUID specialization,String description);
}
