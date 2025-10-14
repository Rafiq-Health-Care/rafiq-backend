package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.request.TestResultRequest;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.service.LabResultService;
import com.nexaworks.rafiq.service.LabService;
import com.nexaworks.rafiq.service.LabTestService;
import com.nexaworks.rafiq.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class LabTestServiceImpl implements LabTestService {
    private final LabService labService;
    private final LabResultService labResultService;
    private final LabTestRepository labTestRepository;
    private final UserService userService;
    @Override
    @Transactional
    public void addTest(TestResultRequest testResultRequest, List<LabResult> entity) {
        LabTest labTest = new LabTest();
        labTest.setName(testResultRequest.name());
        PatientProfile patient = userService.getUser().getPatientProfile();
        labTest.setPatient(patient);
        Lab lab = labService.getLabById(testResultRequest.id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Lab Id"));
        lab.getTests().add(labTest);
        lab = labService.save(lab);
        patient.getLabTests().add(labTest);
        labTest.setLab(lab);
        labTest.setDate(testResultRequest.date()==null? Instant.now(): testResultRequest.date().toInstant());
        labTestRepository.save(labTest);
        entity.forEach(e -> e.setLabTest(labTest));
        labResultService.saveAll(entity);
    }

    @Override
    public Page<LabTest> getAll(int page, int size, String sort, String direction) {
        Sort sorting = Sort.by(Sort.Direction
                .fromString(direction.equalsIgnoreCase("desc")?"desc":"asc"), sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        PatientProfile patient = userService.getUser().getPatientProfile();
        return labTestRepository.findAllByPatientId(patient.getId(),pageable);
    }

    @Override
    public LabTest getTest(UUID testId) {
        return validateOwnership(testId);
    }

    @Override
    public void deleteTest(UUID testId) {
        LabTest test = validateOwnership(testId);
        test.getLab().getTests().remove(test);
        labTestRepository.delete(test);
    }

    @Override
    public Integer deleteAll() {
        PatientProfile patient = userService.getUser().getPatientProfile();
        List<LabTest> tests = patient.getLabTests();
        int size = tests.size();
        labTestRepository.deleteAll(tests);
        return size;
    }

    @Override
    @Transactional
    public void update(UUID testId, TestResultRequest testResultRequest, List<LabResult> entity) {
        LabTest test = validateOwnership(testId);
        updateTestFields(testResultRequest, test);
        updateLabResults(entity, test);
        updateLabAssociation(testResultRequest, test);
        labTestRepository.save(test);
    }

    private void updateLabResults(List<LabResult> entity, LabTest test) {
       labResultService.deleteAll(test.getLabResults());
       entity.forEach(e -> e.setLabTest(test));
       labResultService.saveAll(entity);


    }

    private void updateLabAssociation(TestResultRequest testResultRequest, LabTest test) {
        Lab lab = test.getLab();
        if (lab.getId()!= testResultRequest.id()) {
            Optional<Lab> labOptional = labService.getLabById(testResultRequest.id());
            if (labOptional.isPresent()) {
                lab = labOptional.get();
                lab.getTests().add(test);
                test.setLab(lab);
            } else {
                throw new IllegalArgumentException("Invalid Lab Id");
            }
        }
    }

    private static void updateTestFields(TestResultRequest testResultRequest, LabTest test) {
        test.setName(testResultRequest.name());
        test.setDate(testResultRequest.date()==null? test.getDate(): testResultRequest.date().toInstant());
    }

    @NotNull
    private LabTest validateOwnership(UUID testId) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Test Id"));
        PatientProfile patient = userService.getUser().getPatientProfile();
        if (!test.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Invalid Test Id");
        }
        return test;
    }
}
