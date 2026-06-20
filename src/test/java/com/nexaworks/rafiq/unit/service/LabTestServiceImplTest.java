package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexaworks.rafiq.dto.request.labTest.TestRequest;
import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;
import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.entities.LabTest;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.exception.custom.labtest.LabTestException;
import com.nexaworks.rafiq.mapper.ResultMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.labReports.LabResultServiceImpl;
import com.nexaworks.rafiq.service.labReports.LabTestServiceImpl;
import com.nexaworks.rafiq.service.patient.PatientServiceImpl;
import com.nexaworks.rafiq.service.user.UserServiceImpl;

public class LabTestServiceImplTest {
    @Mock
    LabResultServiceImpl labResultService;

    @Mock
    LabTestRepository labTestRepository;

    @Mock
    PatientRepository patientRepository;

    @Mock
    PatientServiceImpl patientService;

    @Mock
    UserServiceImpl userService;

    @Mock
    TestMapper testMapper;

    @Mock
    ResultMapper resultMapper;

    @InjectMocks
    LabTestServiceImpl labTestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Add test should save lab test and lab results when test is added")
    @Test
    void addTest_ShouldSaveLabTestAndLabResults_WhenTestIsAdded() {
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).build();
        TestResultRequest request = new TestResultRequest("test", LocalDateTime.now(),
                List.of(new TestRequest("CBC", 10.0, "mg", "normal")), labTest.getId());
        // Create Patient directly (Patient extends User)
        Patient patient = Patient.builder().id(UUID.randomUUID()).build();
        List<LabResult> results = new ArrayList<>();

        when(patientService.getPatientProfile()).thenReturn(patient);
        when(labTestRepository.findById(labTest.getId()))
                .thenReturn(java.util.Optional.of(labTest));
        when(labTestRepository.save(labTest)).thenReturn(labTest);
        when(resultMapper.toEntity(request.tests())).thenReturn(results);
        when(labResultService.saveAll(results)).thenReturn(results);

        labTestService.addTest(request);

        verify(labTestRepository, times(1)).findById(labTest.getId());
        verify(labTestRepository, times(1)).save(labTest);
        verify(labTestRepository)
                .save(argThat(labTest1 -> labTest1.getId().equals(labTest.getId())));
        verify(labResultService, times(1)).saveAll(anyList());
    }

    @DisplayName("Add test should save lab test when the test isn't added before")
    @Test
    void addTest_ShouldSaveLabTest_WhenTestIsNotAddedBefore() {
        // Create Patient directly (Patient extends User)
        Patient patient = Patient.builder().id(UUID.randomUUID()).build();
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).build();
        TestResultRequest request = new TestResultRequest("test", LocalDateTime.now(),
                List.of(new TestRequest("CBC", 10.0, "mg", "normal")), labTest.getId());
        List<LabResult> results = new ArrayList<>();
        when(patientService.getPatientProfile()).thenReturn(patient);
        when(labTestRepository.findById(labTest.getId())).thenReturn(java.util.Optional.empty());
        when(labTestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(resultMapper.toEntity(request.tests())).thenReturn(results);
        when(labResultService.saveAll(results)).thenReturn(results);

        labTestService.addTest(request);

        verify(labTestRepository, times(1)).findById(labTest.getId());
        verify(labTestRepository, times(1)).save(any());
        verify(labResultService, times(1)).saveAll(anyList());
        verify(labTestRepository).save(argThat(
                labTest1 -> labTest1.getId() == null && labTest1.getPatient().equals(patient)));
    }

    @DisplayName("Delete test should delete test when the user own this test")
    @Test
    void deleteTest_ShouldDeleteTest_WhenUserOwnIt() {
        UUID testId = UUID.randomUUID();
        // Create Patient directly (Patient extends User)
        Patient patient = Patient.builder().id(UUID.randomUUID()).build();
        LabTest labTest = LabTest.builder().id(testId).patient(patient).build();
        patient.setLabTests(List.of(labTest));
        when(userService.getUserId()).thenReturn(patient.getId());
        when(labTestRepository.findById(testId)).thenReturn(java.util.Optional.of(labTest));

        labTestService.deleteTest(testId);

        verify(labTestRepository, times(1)).findById(testId);
        verify(labTestRepository, times(1)).delete(labTest);
    }

    @DisplayName("Delete should throw exception if the user doesn't own the test")
    @Test
    void deleteTest_ShouldThrowException_WhenUserDoesNotOwnIt() {
        UUID testId = UUID.randomUUID();
        // Create Patient directly (Patient extends User)
        Patient patient = Patient.builder().id(UUID.randomUUID()).build();
        LabTest labTest = LabTest.builder().id(testId)
                .patient(Patient.builder().id(UUID.randomUUID()).build()).build();
        when(patientService.getPatientProfile()).thenReturn(patient);
        when(labTestRepository.findById(testId)).thenReturn(java.util.Optional.of(labTest));

        assertThrows(LabTestException.class, () -> labTestService.deleteTest(testId));
        verify(labTestRepository, never()).delete(any());
    }

    @DisplayName("Delete All test should delete all patient tests and return the number of deleted tests ")
    @Test
    void deleteAllTest_ShouldDeleteAllPatientTests_WhenCalled() {
        // Create Patient directly (Patient extends User)
        Patient patient = Patient.builder().id(UUID.randomUUID()).build();
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).patient(patient).build();
        patient.setLabTests(List.of(labTest));
        when(patientService.getPatientProfile()).thenReturn(patient);
        when(patientRepository.findById(patient.getId()))
                .thenReturn(java.util.Optional.of(patient));

        int deleted = labTestService.deleteAll();
        assertEquals(1, deleted);
    }

}
