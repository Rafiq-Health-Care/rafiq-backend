package com.nexaworks.rafiq.test.labTest.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.labTest.exception.LabTestException;
import com.nexaworks.rafiq.labTest.repository.LabTestRepository;
import com.nexaworks.rafiq.labTest.service.implementation.LabResultServiceImpl;
import com.nexaworks.rafiq.labTest.service.implementation.LabTestServiceImpl;
import com.nexaworks.rafiq.shared.event.labTest.LabTestCreatedEvent;

@DisplayName("LabTestService Test Cases")
class LabTestServiceImplTest {
    @Mock
    LabResultServiceImpl labResultService;

    @Mock
    LabTestRepository labTestRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    LabTestServiceImpl labTestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TransactionSynchronizationManager.initSynchronization();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    private void triggerTransactionSynchronization() {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> {
            try {
                sync.afterCommit();
            } catch (Exception e) {
                // Ignore exceptions in test
            }
        });
    }

    @DisplayName("Add test should save lab test and lab results when test is added")
    @Test
    void addTest_ShouldSaveLabTestAndLabResults_WhenTestIsAdded() {
        UUID patientId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID testId = UUID.randomUUID();
        LabTest savedLabTest = LabTest.builder().id(testId).fileId(fileId).patientId(patientId)
                .build();

        when(labTestRepository.save(any(LabTest.class))).thenReturn(savedLabTest);
        when(labResultService.saveAll(anyList())).thenReturn(new ArrayList<>());

        labTestService.addTest(fileId, "test", new Date(), new ArrayList<>(), patientId);
        triggerTransactionSynchronization();

        verify(labTestRepository, times(1))
                .save(argThat(savedTest -> savedTest.getFileId().equals(fileId)
                        && savedTest.getPatientId().equals(patientId)
                        && savedTest.getName().equals("test")));
        verify(labResultService, times(1)).saveAll(anyList());

        ArgumentCaptor<LabTestCreatedEvent> eventCaptor = ArgumentCaptor
                .forClass(LabTestCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        LabTestCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(fileId, capturedEvent.fileId());
        assertEquals(testId, capturedEvent.testId());
    }

    @DisplayName("Add test should save lab test when the test isn't added before")
    @Test
    void addTest_ShouldSaveLabTest_WhenTestIsNotAddedBefore() {
        UUID patientId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        UUID testId = UUID.randomUUID();

        when(labTestRepository.save(any(LabTest.class))).thenAnswer(invocation -> {
            LabTest test = invocation.getArgument(0);
            test.setId(testId);
            return test;
        });
        when(labResultService.saveAll(anyList())).thenReturn(new ArrayList<>());

        labTestService.addTest(fileId, "test", new Date(), new ArrayList<>(), patientId);
        triggerTransactionSynchronization();

        verify(labTestRepository, times(1))
                .save(argThat(savedTest -> savedTest.getFileId().equals(fileId)
                        && savedTest.getPatientId().equals(patientId)
                        && savedTest.getName().equals("test")));
        verify(labResultService, times(1)).saveAll(anyList());

        ArgumentCaptor<LabTestCreatedEvent> eventCaptor = ArgumentCaptor
                .forClass(LabTestCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        LabTestCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(fileId, capturedEvent.fileId());
        assertEquals(testId, capturedEvent.testId());
    }

    @DisplayName("Delete test should delete test when the user own this test")
    @Test
    void deleteTest_ShouldDeleteTest_WhenUserOwnIt() {
        UUID testId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        LabTest labTest = LabTest.builder().id(testId).patientId(patientId).build();

        when(labTestRepository.findById(testId)).thenReturn(java.util.Optional.of(labTest));

        labTestService.deleteTest(testId, patientId);

        verify(labTestRepository, times(1)).findById(testId);
        verify(labTestRepository, times(1)).delete(labTest);
    }

    @DisplayName("Delete should throw exception if the user doesn't own the test")
    @Test
    void deleteTest_ShouldThrowException_WhenUserDoesNotOwnIt() {
        UUID testId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID otherPatientId = UUID.randomUUID();
        LabTest labTest = LabTest.builder().id(testId).patientId(otherPatientId).build();

        when(labTestRepository.findById(testId)).thenReturn(java.util.Optional.of(labTest));

        assertThrows(LabTestException.class, () -> labTestService.deleteTest(testId, patientId));
        verify(labTestRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("Delete All test should delete all patient tests and return the number of deleted tests ")
    @Test
    void deleteAllTest_ShouldDeleteAllPatientTests_WhenCalled() {
        UUID patientId = UUID.randomUUID();
        LabTest labTest = LabTest.builder().id(UUID.randomUUID()).patientId(patientId).build();

        when(labTestRepository.findAllByPatientId(patientId)).thenReturn(List.of(labTest));
        doNothing().when(labTestRepository).deleteAll(anyList());

        int deleted = labTestService.deleteAll(patientId);
        assertEquals(1, deleted);
        verify(labTestRepository, times(1)).findAllByPatientId(patientId);
        verify(labTestRepository, times(1)).deleteAll(anyList());
    }
}
