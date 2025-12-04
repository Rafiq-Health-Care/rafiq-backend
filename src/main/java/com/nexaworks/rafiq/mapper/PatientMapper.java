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
    @Mapping(target = "bmi", expression = "java(getBmiStatus(patient.getWeight(), patient.getHeight()))")
    PatientProfileResponse toResponse(Patient patient);

    default String getBmiStatus(double weight, int height) {
        int bmi = (int) (weight / (height * height));
        return bmi < 18.5 ? "Underweight" : bmi >= 18.5 && bmi < 25 ? "Normal" : "Overweight";
    }

    @Mapping(target = "patientProfile", expression = "java(this.toResponse(patient))")
    @Mapping(target = "tests", expression = "java(patient.getLabTests().stream().map(testMapper::toResponse).toList())")
    @Mapping(target = "medicines", expression = "java(patient.getMedicines().stream().map(medicineMapper::toPreviewDto).toList())")
    CompletePatientProfile convertToCompleteProfile(Patient patient, @Context TestMapper testMapper,
            @Context MedicineMapper medicineMapper);
}
