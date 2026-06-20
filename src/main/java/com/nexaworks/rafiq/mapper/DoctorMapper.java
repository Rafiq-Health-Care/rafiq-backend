package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorStatsDTO;
import com.nexaworks.rafiq.entities.Doctor;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(target = "yearsOfExperience", source = "doctor.experienceYears")
    @Mapping(target = "nextAvailable", expression = "java(doctorStatsDTO != null ? doctorStatsDTO.startTime() : null)")
    @Mapping(target = "consultationCount", expression = "java(doctorStatsDTO != null ? doctorStatsDTO.consultationsCount() : null)")
    DoctorProfileResponse toProfileResponse(Doctor doctor, DoctorStatsDTO doctorStatsDTO);
}
