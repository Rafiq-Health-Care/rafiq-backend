package com.nexaworks.rafiq.service;

import java.util.UUID;

import com.nexaworks.rafiq.dto.UploadResults;
import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.User;

public interface DoctorService {
    DoctorProfile createProfile(User doctor, String description, UUID specialization);

    void updateNationalId(UploadResults uploadResults, UUID uuid);
}
