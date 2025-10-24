package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.dto.request.TestResultRequest;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.exception.custom.LabException;
import com.nexaworks.rafiq.exception.custom.LabTestException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class LabTestServiceImpl implements LabTestService {
    private final LabService labService;
    private final LabResultService labResultService;
    private final LabTestRepository labTestRepository;
    private final UserService userService;
    private final ImageService imageService;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public void addTest(TestResultRequest testResultRequest, List<LabResult> entity) {
        LabTest labTest = getLabTest(testResultRequest);
        setTestFields(labTest, testResultRequest, Instant.now());
        PatientProfile patient = getPatientProfile();
        labTest.setPatient(patient);
        Lab lab = getLab(testResultRequest);
        lab.getTests().add(labTest);
        lab = labService.save(lab);
        labTest.setLab(lab);
        labTestRepository.save(labTest);
        entity.forEach(e -> e.setLabTest(labTest));
        labResultService.saveAll(entity);
    }


    protected PatientProfile getPatientProfile() {
        return userService.getUser().getPatientProfile();
    }

    private Lab getLab(TestResultRequest testResultRequest) {
        return labService.getLabById(testResultRequest.id())
                .orElseThrow(() -> new LabException("Invalid Lab Id"));
    }


    private static void setTestFields(LabTest labTest, TestResultRequest testResultRequest, Instant now) {
        labTest.setName(testResultRequest.name());
        labTest.setDate(testResultRequest.date() == null ? now : testResultRequest.date().toInstant());
    }

    @NotNull
    private LabTest getLabTest(TestResultRequest testResultRequest) {
        LabTest labTest = labTestRepository.findById(testResultRequest.testId())
                .orElseGet(LabTest::new);
        log.info("Test Id {}",labTest.getId());
        return labTest;
    }


    @Override
    public Page<LabTest> getAll(int page, int size, String sort, String direction) {
        Sort sorting = Sort.by(Sort.Direction
                .fromString(direction.equalsIgnoreCase("desc")?"desc":"asc"), sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        PatientProfile patient = getPatientProfile();
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
    @Transactional
    public Integer deleteAll() {
        PatientProfile patient = getPatientProfile();
        patient = patientRepository.findById(patient.getId()).orElseThrow(()->
                new UserNotFoundException("Invalid Patient Id"));
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

    @Transactional
    protected void updateLabResults(List<LabResult> entity, LabTest test) {
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
                throw new LabException("Invalid Lab Id");
            }
        }
    }

    private static void updateTestFields(TestResultRequest testResultRequest, LabTest test) {
        setTestFields(test, testResultRequest, test.getDate());
    }

    @NotNull
    private LabTest validateOwnership(UUID testId) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new LabTestException("Invalid Test Id"));
        PatientProfile patient = getPatientProfile();
        if (!test.getPatient().getId().equals(patient.getId())) {
            throw new LabTestException("Invalid Test Id");
        }
        return test;
    }
    @Override
    @Async
    public CompletableFuture<UUID> saveTestPdf(MultipartFile file,User user) throws IOException {
        String fileType = file.getContentType();
        List<String> result;
        if (fileType!=null&&fileType.startsWith("image/")) {
            result=imageService.uploadFile(file);
        } else {
            result = imageService.uploadPdf(file);
        }
        LabTest labTest = new LabTest();
        labTest.setPdf(result.get(0));
        labTest.setPublicId(result.get(1));
        labTest.setFileType(fileType);
        PatientProfile patient = user.getPatientProfile();
        labTest.setPatient(patient);
       labTest = labTestRepository.save(labTest);
        return CompletableFuture.completedFuture(labTest.getId());
    }
}
