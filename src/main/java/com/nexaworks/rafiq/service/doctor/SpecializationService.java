package com.nexaworks.rafiq.service.doctor;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.dto.response.specialization.SpecializationResponse;
import com.nexaworks.rafiq.entities.enums.Specialization;

public interface SpecializationService {




    List<Specialization> getSpecializations();


}
