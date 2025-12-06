package com.nexaworks.rafiq.labTest.service.implementation;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexaworks.rafiq.labTest.entity.LabResult;
import com.nexaworks.rafiq.labTest.entity.LabTest;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.labTest.api.dto.TestResultRequest;
import com.nexaworks.rafiq.labTest.exception.LabTestException;
import com.nexaworks.rafiq.labTest.repository.LabTestRepository;
import com.nexaworks.rafiq.labTest.service.LabResultService;
import com.nexaworks.rafiq.labTest.service.LabTestService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabTestServiceImpl implements LabTestService {
    private final LabResultService labResultService;
    private final LabTestRepository labTestRepository;


    @Override
    @Transactional
    public void addTest(UUID testId, String testName, Date testDate, List<LabResult> entity,UUID patientId) {
        LabTest labTest = getLabTest(Optional.of(testId));
        setTestFields(labTest, testName, testDate);
        labTest.setPatientId(patientId);
        labTestRepository.save(labTest);
        entity.forEach(e -> e.setLabTest(labTest));
        labResultService.saveAll(entity);
    }

    private static void setTestFields(LabTest labTest, String testName, Date testDate) {
        labTest.setName(testName);
        labTest.setDate(testDate == null ? Instant.now() : testDate.toInstant());
    }

    @NotNull
    private LabTest getLabTest(Optional<UUID> testId) {
        return testId.map(id -> labTestRepository.findById(id).orElse(new LabTest()))
                .orElse(new LabTest());
    }

    @Override
    public Page<LabTest> getAll(int page, int size, String sort, String direction,UUID patientId) {
        Sort sorting = Sort.by(
                Sort.Direction.fromString(direction.equalsIgnoreCase("desc") ? "desc" : "asc"),
                sort);

        Pageable pageable = PageRequest.of(page, size, sorting);
        
        return labTestRepository.findAllByPatientId(patientId, pageable);
    }

    @Override
    public LabTest getTest(UUID testId,UUID patientId) {
        return validateOwnership(testId,patientId);
    }

    @Override
    public void deleteTest(UUID testId,UUID patientId) {
        LabTest test = validateOwnership(testId,patientId);
        labTestRepository.delete(test);
    }

    @Override
    @Transactional
    public Integer deleteAll(UUID patientId) {
        List<LabTest> tests = labTestRepository.findAllByPatientId(patientId);
        int size = tests.size();
        labTestRepository.deleteAll(tests);
        return size;
    }

    @Override
    @Transactional
    public void update(UUID testId, TestResultRequest testResultRequest, List<LabResult> entity,UUID patientId) {
        LabTest test = validateOwnership(testId,patientId);
        setTestFields(test, testResultRequest.name(), testResultRequest.date());
        updateLabResults(entity, test);
        labTestRepository.save(test);
    }

    @Transactional
    protected void updateLabResults(List<LabResult> entity, LabTest test) {
        labResultService.deleteAll(test.getLabResults());
        entity.forEach(e -> e.setLabTest(test));
        labResultService.saveAll(entity);
    }

    @NotNull
    private LabTest validateOwnership(UUID testId,UUID patientId) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new LabTestException("Invalid Test Id"));
        if (!test.getId().equals(patientId)) {
            throw new LabTestException("Invalid Test Id");
        }
        return test;
    }


}
