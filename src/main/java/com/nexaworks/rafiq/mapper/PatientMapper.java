package com.nexaworks.rafiq.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.entities.Patient;

@Mapper(componentModel = "spring", uses = {TestMapper.class, MedicineMapper.class})
public interface PatientMapper {
    @Mapping(source = "id", target = "patientId")
    PatientProfileResponse toResponse(Patient patient);

    @Mapping(target = "patientProfile", expression = "java(this.toResponse(patient))")
    @Mapping(target = "tests", expression = "java(patient.getLabTests().stream().map(testMapper::toResponse).toList())")
    @Mapping(target = "medicines", expression = "java(patient.getMedicines().stream().map(medicineMapper::toPreviewDto).toList())")
    CompletePatientProfile convertToCompleteProfile(Patient patient, @Context TestMapper testMapper,
            @Context MedicineMapper medicineMapper);
}
