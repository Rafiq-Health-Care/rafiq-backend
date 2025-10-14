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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
        labTestRepository.save(labTest);
        entity.forEach(e -> e.setLabTest(labTest));
        labResultService.saveAll(entity);
    }
}
