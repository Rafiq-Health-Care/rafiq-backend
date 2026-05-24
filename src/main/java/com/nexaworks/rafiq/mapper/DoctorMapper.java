package com.nexaworks.rafiq.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse;
import com.nexaworks.rafiq.dto.response.doctor.EducationItemResponse;
import com.nexaworks.rafiq.dto.response.doctor.ExperienceItemResponse;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Education;
import com.nexaworks.rafiq.entities.Experience;

@Mapper(componentModel = "spring")
public interface DoctorMapper {

    EducationItemResponse toEducationDto(Education education);

    ExperienceItemResponse toExperienceDto(Experience experience);

    Education toEducationEntity(EducationItemRequest request);

    Experience toExperienceEntity(ExperienceItemRequest request);

    default DoctorProfileResponse toProfileResponse(Doctor doctor) {
        List<EducationItemResponse> edu = doctor.getEducation() == null
                ? List.of()
                : doctor.getEducation().stream().map(this::toEducationDto).toList();
        List<ExperienceItemResponse> exp = doctor.getExperience() == null
                ? List.of()
                : doctor.getExperience().stream().map(this::toExperienceDto).toList();
        return new DoctorProfileResponse(doctor.getId(), doctor.getName(),
                doctor.getPersonalPhoto(), doctor.getBiography(), doctor.getDescription(),
                doctor.getPrice(), doctor.getSpecialization(), edu, exp);
    }
}
