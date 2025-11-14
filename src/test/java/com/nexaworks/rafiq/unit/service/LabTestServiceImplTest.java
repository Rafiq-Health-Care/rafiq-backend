package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.nexaworks.rafiq.entities.LabTest;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.LabTestException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.LabTestRepository;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.ImageService;
import com.nexaworks.rafiq.service.ServiceImpl.LabResultServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.LabTestServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.PatientServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.UserServiceImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LabTestServiceImplTest {
    @Mock
    LabResultServiceImpl labResultService;

    @Mock
    LabTestRepository labTestRepository;

    @Mock
    ImageService imageService;

    @Mock
    PatientRepository patientRepository;

    @Mock
    PatientServiceImpl patientService;

    @Mock
    UserServiceImpl userService;

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
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();

        when(userService.getUser()).thenReturn(user);
        when(labTestRepository.findById(labTest.getId())).thenReturn(java.util.Optional.of(labTest));
        when(labTestRepository.save(labTest)).thenReturn(labTest);
        when(labResultService.saveAll(labTest.getLabResults())).thenReturn(labTest.getLabResults());

        labTestService.addTest(labTest.getId(), "test", new Date(), new ArrayList<>());

        verify(labTestRepository, times(1)).findById(labTest.getId());
        verify(labTestRepository, times(1)).save(labTest);
        verify(labTestRepository).save(argThat(labTest1 -> labTest1.getId().equals(labTest.getId())));
        verify(labResultService, times(1)).saveAll(anyList());
    }

    @DisplayName("Add test should save lab test when the test isn't added before")
    @Test
    void addTest_ShouldSaveLabTest_WhenTestIsNotAddedBefore() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).build();
        when(userService.getUser()).thenReturn(user);
        when(labTestRepository.findById(labTest.getId())).thenReturn(java.util.Optional.empty());
        when(labTestRepository.save(any())).thenReturn(any());
        when(labResultService.saveAll(labTest.getLabResults())).thenReturn(labTest.getLabResults());

        labTestService.addTest(labTest.getId(), "test", new Date(), new ArrayList<>());

        verify(labTestRepository, times(1)).findById(labTest.getId());
        verify(labTestRepository, times(1)).save(any());
        verify(labResultService, times(1)).saveAll(anyList());
        verify(labTestRepository)
                .save(argThat(labTest1 ->
                        labTest1.getId() == null && labTest1.getPatient().equals(user.getPatientProfile())));
    }

    @DisplayName("Delete test should delete test when the user own this test")
    @Test
    void deleteTest_ShouldDeleteTest_WhenUserOwnIt() {
        UUID testId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();
        LabTest labTest =
                LabTest.builder().id(testId).patient(user.getPatientProfile()).build();
        user.getPatientProfile().setLabTests(List.of(labTest));
        when(userService.getUser()).thenReturn(user);
        when(labTestRepository.findById(testId)).thenReturn(java.util.Optional.of(labTest));

        labTestService.deleteTest(testId);

        verify(labTestRepository, times(1)).findById(testId);
        verify(labTestRepository, times(1)).delete(labTest);
    }

    @DisplayName("Delete should throw exception if the user doesn't own the test")
    @Test
    void deleteTest_ShouldThrowException_WhenUserDoesNotOwnIt() {
        UUID testId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();
        LabTest labTest = LabTest.builder()
                .id(testId)
                .patient(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();
        when(userService.getUser()).thenReturn(user);
        when(labTestRepository.findById(testId)).thenReturn(java.util.Optional.of(labTest));

        assertThrows(LabTestException.class, () -> labTestService.deleteTest(testId));
        verify(labTestRepository, never()).delete(any());
    }

    @DisplayName("Delete All test should delete all patient tests and return the number of deleted tests ")
    @Test
    void deleteAllTest_ShouldDeleteAllPatientTests_WhenCalled() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();
        LabTest labTest = LabTest.builder()
                .id(UUID.randomUUID())
                .patient(user.getPatientProfile())
                .build();
        user.getPatientProfile().setLabTests(List.of(labTest));
        when(userService.getUser()).thenReturn(user);
        when(patientRepository.findById(user.getPatientProfile().getId()))
                .thenReturn(java.util.Optional.of(user.getPatientProfile()));

        int deleted = labTestService.deleteAll();
        assertEquals(1, deleted);
    }

    @DisplayName("Delete All test should throw exception if patient not found")
    @Test
    void deleteAllTest_ShouldThrowException_WhenPatientNotFound() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .patientProfile(PatientProfile.builder().id(UUID.randomUUID()).build())
                .build();
        when(userService.getUser()).thenReturn(user);
        when(patientRepository.findById(user.getPatientProfile().getId())).thenReturn(java.util.Optional.empty());
        assertThrows(UserNotFoundException.class, () -> labTestService.deleteAll());
    }
}
