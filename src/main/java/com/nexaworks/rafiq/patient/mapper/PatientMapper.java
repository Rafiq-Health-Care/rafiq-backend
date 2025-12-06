package com.nexaworks.rafiq.patient.mapper;

import com.nexaworks.rafiq.medication.mapper.MedicineMapper;
import com.nexaworks.rafiq.labTest.mapper.TestMapper;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.patient.api.dto.response.CompletePatientDataResponse;

import com.nexaworks.rafiq.patient.entity.model.Patient;

@Mapper(componentModel = "spring", uses = {TestMapper.class, MedicineMapper.class})
public interface PatientMapper {
    @Mapping(source = "id", target = "patientId")
    @Mapping(target = "bmi", expression = "java(getBmiStatus(patient.getWeight(), patient.getHeight()))")
    CompletePatientDataResponse toResponse(Patient patient);

    default String getBmiStatus(double weight, int height) {
        int bmi = (int) (weight / (height * height));
        return bmi < 18.5 ? "Underweight" : bmi >= 18.5 && bmi < 25 ? "Normal" : "Overweight";
    }

    @Mapping(target = "patientProfile", expression = "java(this.toResponse(patient))")
    @Mapping(target = "tests", expression = "java(patient.getLabTests().stream().map(testMapper::toResponse).toList())")
    @Mapping(target = "medicines", expression = "java(patient.getMedicines().stream().map(medicineMapper::toPreviewDto).toList())")
    CompletePatientDataResponse convertToCompleteProfile(Patient patient, @Context TestMapper testMapper,
                                                         @Context MedicineMapper medicineMapper);
}
