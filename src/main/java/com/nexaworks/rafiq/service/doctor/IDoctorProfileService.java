package com.nexaworks.rafiq.service.doctor;

import java.util.UUID;

import com.nexaworks.rafiq.dto.request.doctor.AddNewExperience;
import com.nexaworks.rafiq.dto.request.doctor.EditBiographyRequest;
import com.nexaworks.rafiq.dto.request.doctor.EditExperience;
import com.nexaworks.rafiq.dto.request.doctor.UpdateBasicInfoRequest;

import jakarta.validation.Valid;

public interface IDoctorProfileService {
    void updateBiography(@Valid EditBiographyRequest request);

    void updateBasicInfo(@Valid UpdateBasicInfoRequest request);

    void addNewExperience(@Valid AddNewExperience request);

    void editExperience(UUID expId, @Valid EditExperience request);
}
