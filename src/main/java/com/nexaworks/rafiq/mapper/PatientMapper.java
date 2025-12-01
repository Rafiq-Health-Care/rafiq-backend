package com.nexaworks.rafiq.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.entities.PatientProfile;

@Mapper(componentModel = "spring", uses = {TestMapper.class, MedicineMapper.class})
public interface PatientMapper {
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.phone", target = "phoneNumber")
    @Mapping(source = "id", target = "patientId")
    @Mapping(target = "bmi", expression = "java(getBmiStatus(patient.getWeight(), patient.getHeight()))")
    PatientProfileResponse toResponse(PatientProfile patient);

    default String getBmiStatus(int weight, int height) {
        int bmi = (int) (weight / (height * height));
        return bmi < 18.5 ? "Underweight" : bmi >= 18.5 && bmi < 25 ? "Normal" : "Overweight";
    }

    @Mapping(target = "patientProfile", expression = "java(this.toResponse(patientProfile))")
    @Mapping(target = "tests", expression = "java(patientProfile.getLabTests().stream().map(testMapper::toResponse).toList())")
    @Mapping(target = "medicines", expression = "java(patientProfile.getMedicines().stream().map(medicineMapper::toPreviewDto).toList())")
    CompletePatientProfile convertToCompleteProfile(PatientProfile patientProfile,
            @Context TestMapper testMapper, @Context MedicineMapper medicineMapper);
}
