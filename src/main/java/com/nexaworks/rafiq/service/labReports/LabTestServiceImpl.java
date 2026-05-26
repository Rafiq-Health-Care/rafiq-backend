package com.nexaworks.rafiq.service.labReports;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.labTest.TestResponse;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.entities.enums.UploadType;
import com.nexaworks.rafiq.exception.custom.labtest.LabTestException;
import com.nexaworks.rafiq.mapper.ResultMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.service.file.ImageService;
import com.nexaworks.rafiq.service.patient.PatientService;
import com.nexaworks.rafiq.service.user.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabTestServiceImpl implements LabTestService {
    private final LabResultService labResultService;
    private final LabTestRepository labTestRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final PatientService patientService;
    private final TestMapper testMapper;
    private final ResultMapper resultMapper;

    @Override
    @Transactional
    public void addTest(TestResultRequest request) {
        LabTest labTest = getLabTest(Optional.ofNullable(request.testId()));
        setTestFields(labTest, request.name(), request.date());
        List<LabResult> entity = resultMapper.toEntity(request.tests());
        Patient patient = patientService.getPatientProfile();
        labTest.setPatient(patient);
        labTestRepository.save(labTest);
        entity.forEach(e -> e.setLabTest(labTest));
        labResultService.saveAll(entity);
    }

    private static void setTestFields(LabTest labTest, String testName, LocalDateTime testDate) {
        labTest.setName(testName);
        labTest.setDate(testDate == null ? LocalDateTime.now() : testDate);
    }

    @NotNull
    private LabTest getLabTest(Optional<UUID> testId) {
        return testId.map(id -> labTestRepository.findById(id).orElse(new LabTest()))
                .orElse(new LabTest());
    }

    @Override
    public PageResponse<TestResponse> getAll(int page, int size, String sort, String direction) {
        Sort sorting = Sort.by(
                Sort.Direction.fromString(direction.equalsIgnoreCase("desc") ? "desc" : "asc"),
                sort);

        Pageable pageable = PageRequest.of(page, size, sorting);
        UUID patientId = userService.getUserId();
        Page<LabTest> tests = labTestRepository.findAllByPatientId(patientId, pageable);
        return PageResponse.of(tests, testMapper::toResponse);
    }

    @Override
    public com.nexaworks.rafiq.dto.response.labTest.TestResultsResponse getTest(UUID testId) {
        LabTest test = validateOwnership(testId);
        return testMapper.mapToTestResponse(test);
    }

    @Override
    public void deleteTest(UUID testId) {
        LabTest test = validateOwnership(testId);
        labTestRepository.delete(test);
    }

    @Override
    @Transactional
    public Integer deleteAll() {
        Patient patient = patientService.getPatientProfile();
        List<LabTest> tests = patient.getLabTests();
        int size = tests.size();
        labTestRepository.deleteAll(tests);
        return size;
    }

    @Override
    @Transactional
    public void update(UUID testId, TestResultRequest testResultRequest) {
        LabTest test = validateOwnership(testId);
        List<LabResult> entity = resultMapper.toEntity(testResultRequest.tests());
        setTestFields(test, testResultRequest.name(), testResultRequest.date());
        updateLabResults(entity, test);
    }

    @Transactional
    protected void updateLabResults(List<LabResult> entity, LabTest test) {
        List<LabResult> existingResults = test.getLabResults();
        if (existingResults != null && !existingResults.isEmpty()) {
            labResultService.deleteAll(existingResults);
            existingResults.clear();
        }
        entity.forEach(e -> e.setLabTest(test));
        test.setLabResults(entity);
        labResultService.saveAll(entity);
    }

    @NotNull
    private LabTest validateOwnership(UUID testId) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new LabTestException("Invalid Test Id"));
        UUID patientId = userService.getUserId();
        if (!test.getPatient().getId().equals(patientId)) {
            throw new LabTestException("Invalid Test Id");
        }
        return test;
    }

    @Override
    @Async
    public CompletableFuture<UUID> saveTestPdf(MultipartFile file, User user) throws IOException {
        String fileType = file.getContentType();
        UploadResults result;
        if (fileType != null && fileType.startsWith("image/")) {
            result = imageService.uploadResource(file, UploadType.IMAGE);
        } else {
            result = imageService.uploadResource(file, UploadType.PDF);
        }
        LabTest labTest = new LabTest();
        labTest.setPdf(result.url());
        labTest.setPublicId(result.publicId());
        labTest.setFileType(fileType);
        labTest.setPatient((Patient) user);
        labTest = labTestRepository.save(labTest);
        return CompletableFuture.completedFuture(labTest.getId());
    }
}
