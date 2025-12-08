package com.nexaworks.rafiq.patient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.labTest.mapper.TestMapper;
import com.nexaworks.rafiq.medication.mapper.MedicineMapper;
import com.nexaworks.rafiq.patient.api.dto.response.CompletePatientDataResponse;
import com.nexaworks.rafiq.patient.entity.model.Patient;

@Mapper(componentModel = "spring", uses = {TestMapper.class, MedicineMapper.class})
public interface PatientMapper {
    @Mapping(target = "bmi", expression = "java(getBmiStatus(patient.getWeight(), patient.getHeight()))")
    @Mapping(target = "patientId", source = "id")
    CompletePatientDataResponse toResponse(Patient patient);

    default String getBmiStatus(double weight, int height) {
        int bmi = (int) (weight / (height * height));
        return bmi < 18.5 ? "Underweight" : bmi >= 18.5 && bmi < 25 ? "Normal" : "Overweight";
    }

}
