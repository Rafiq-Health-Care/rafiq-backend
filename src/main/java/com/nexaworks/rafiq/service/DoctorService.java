package com.nexaworks.rafiq.service;

import java.util.UUID;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.User;

public interface DoctorService {
    Doctor createProfile(User doctor, String description, UUID specialization);

    void updateNationalId(UploadResults uploadResults, UUID uuid);
}
