package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.enums.Color;
import com.nexaworks.rafiq.exception.custom.GroupIsAlreadyExistsException;
import com.nexaworks.rafiq.exception.custom.GroupNotFoundException;
import com.nexaworks.rafiq.exception.custom.MedicineNotFound;
import com.nexaworks.rafiq.repository.GroupRepository;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.ServiceImpl.GroupServiceImpl;

@DisplayName("GroupService Unit Tests")
public class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Patient patient;
    private UUID patientId;
    private UUID groupId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        groupId = UUID.randomUUID();
        patient = Patient.builder().id(patientId).build();
    }

    @Nested
    @DisplayName("Get Group By ID Tests")
    class GetGroupByIdTests {

        @Test
        @DisplayName("Should return group successfully when group exists and belongs to patient")
        void getGroupById_ShouldReturnGroup_WhenGroupExistsAndBelongsToPatient() {
            // Arrange
            Group group = Group.builder().id(groupId).name("Pain Relief")
                    .description("Pain medications").patient(patient).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(patientService.getPatientProfile()).thenReturn(patient);

            // Act
            Group result = groupService.getGroupById(groupId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(groupId);
            assertThat(result.getName()).isEqualTo("Pain Relief");
            verify(groupRepository, times(1)).findById(groupId);
            verify(patientService, times(1)).getPatientProfile();
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group does not exist")
        void getGroupById_ShouldThrowGroupNotFoundException_WhenGroupDoesNotExist() {
            // Arrange
            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatExceptionOfType(GroupNotFoundException.class)
                    .isThrownBy(() -> groupService.getGroupById(groupId))
                    .withMessageContaining("Group not found with id: " + groupId);

            verify(groupRepository, times(1)).findById(groupId);
            verify(patientService, never()).getPatientProfile();
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group belongs to different patient")
        void getGroupById_ShouldThrowGroupNotFoundException_WhenGroupBelongsToDifferentPatient() {
            // Arrange
            UUID differentPatientId = UUID.randomUUID();
            Patient differentPatient = Patient.builder().id(differentPatientId).build();

            Group group = Group.builder().id(groupId).name("Pain Relief").patient(differentPatient)
                    .build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(patientService.getPatientProfile()).thenReturn(patient);

            // Act & Assert
            assertThatExceptionOfType(GroupNotFoundException.class)
                    .isThrownBy(() -> groupService.getGroupById(groupId))
                    .withMessageContaining("Group not found with id: " + groupId);

            verify(groupRepository, times(1)).findById(groupId);
            verify(patientService, times(1)).getPatientProfile();
        }
    }

    @Nested
    @DisplayName("Add Group Tests")
    class AddGroupTests {

        @Test
        @DisplayName("Should add group successfully when name is unique")
        void addGroup_ShouldAddGroupSuccessfully_WhenNameIsUnique() {
            // Arrange
            Group group = Group.builder().name("Vitamins").description("Vitamin supplements")
                    .color(Color.BLUE).build();

            Group savedGroup = Group.builder().id(groupId).name("Vitamins")
                    .description("Vitamin supplements").color(Color.BLUE).patient(patient).build();

            when(groupRepository.existsGroupByName("Vitamins")).thenReturn(false);
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

            // Act
            Group result = groupService.addGroup(group);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(groupId);
            assertThat(result.getName()).isEqualTo("Vitamins");
            assertThat(result.getPatient()).isEqualTo(patient);
            verify(groupRepository, times(1)).existsGroupByName("Vitamins");
            verify(patientService, times(1)).getPatientProfile();
            verify(groupRepository, times(1)).save(any(Group.class));
        }

        @Test
        @DisplayName("Should throw GroupIsAlreadyExistsException when group name already exists")
        void addGroup_ShouldThrowGroupIsAlreadyExistsException_WhenGroupNameAlreadyExists() {
            // Arrange
            Group group = Group.builder().name("Vitamins").description("Vitamin supplements")
                    .build();

            when(groupRepository.existsGroupByName("Vitamins")).thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(GroupIsAlreadyExistsException.class)
                    .isThrownBy(() -> groupService.addGroup(group))
                    .withMessageContaining("Group with name Vitamins already exists");

            verify(groupRepository, times(1)).existsGroupByName("Vitamins");
            verify(patientService, never()).getPatientProfile();
            verify(groupRepository, never()).save(any(Group.class));
        }
    }

    @Nested
    @DisplayName("Get Groups Tests")
    class GetGroupsTests {

        @Test
        @DisplayName("Should return paginated groups with ascending sort")
        void getGroups_ShouldReturnPaginatedGroups_WithAscendingSort() {
            // Arrange
            List<Group> groups = new ArrayList<>();
            groups.add(
                    Group.builder().id(UUID.randomUUID()).name("Group A").patient(patient).build());
            groups.add(
                    Group.builder().id(UUID.randomUUID()).name("Group B").patient(patient).build());

            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, groups.size());

            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            Page<Group> result = groupService.getGroups(0, 10, "asc", "name");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().size()).isEqualTo(2);
            verify(patientService, times(1)).getPatientProfile();
            verify(groupRepository, times(1)).findByPatientId(eq(patientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated groups with descending sort")
        void getGroups_ShouldReturnPaginatedGroups_WithDescendingSort() {
            // Arrange
            List<Group> groups = new ArrayList<>();
            groups.add(
                    Group.builder().id(UUID.randomUUID()).name("Group B").patient(patient).build());
            groups.add(
                    Group.builder().id(UUID.randomUUID()).name("Group A").patient(patient).build());

            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, groups.size());

            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            Page<Group> result = groupService.getGroups(0, 10, "desc", "name");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().size()).isEqualTo(2);
            verify(patientService, times(1)).getPatientProfile();
            verify(groupRepository, times(1)).findByPatientId(eq(patientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when patient has no groups")
        void getGroups_ShouldReturnEmptyPage_WhenPatientHasNoGroups() {
            // Arrange
            List<Group> groups = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, 0);

            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            Page<Group> result = groupService.getGroups(0, 10, "asc", "name");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent().size()).isEqualTo(0);
            verify(patientService, times(1)).getPatientProfile();
            verify(groupRepository, times(1)).findByPatientId(eq(patientId), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Update Group By ID Tests")
    class UpdateGroupByIdTests {

        @Test
        @DisplayName("Should update group successfully when request is valid")
        void updateGroupById_ShouldUpdateGroupSuccessfully_WhenRequestIsValid() {
            // Arrange
            Group existingGroup = Group.builder().id(groupId).name("Old Name")
                    .description("Old Description").color(Color.BLUE).patient(patient).build();

            UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Description",
                    Color.RED);

            Group updatedGroup = Group.builder().id(groupId).name("New Name")
                    .description("New Description").color(Color.RED).patient(patient).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.existsGroupByName("New Name")).thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenReturn(updatedGroup);

            // Act
            Group result = groupService.updateGroupById(request, groupId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getDescription()).isEqualTo("New Description");
            assertThat(result.getColor()).isEqualTo(Color.RED);
            verify(groupRepository, times(1)).save(any(Group.class));
        }

        @Test
        @DisplayName("Should throw GroupIsAlreadyExistsException when updating to existing group name")
        void updateGroupById_ShouldThrowGroupIsAlreadyExistsException_WhenUpdatingToExistingName() {
            // Arrange
            Group existingGroup = Group.builder().id(groupId).name("Old Name")
                    .description("Old Description").patient(patient).build();

            UpdateGroupRequest request = new UpdateGroupRequest("Existing Name", "New Description",
                    Color.RED);

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.existsGroupByPatient_IdAndName(any(), any())).thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(GroupIsAlreadyExistsException.class)
                    .isThrownBy(() -> groupService.updateGroupById(request, groupId))
                    .withMessageContaining("Group with name Existing Name already exists");

            verify(groupRepository, never()).save(any(Group.class));
        }

        @Test
        @DisplayName("Should update only provided fields when partial update request")
        void updateGroupById_ShouldUpdateOnlyProvidedFields_WhenPartialUpdateRequest() {
            // Arrange
            Group existingGroup = Group.builder().id(groupId).name("Old Name")
                    .description("Old Description").color(Color.BLUE).patient(patient).build();

            UpdateGroupRequest request = new UpdateGroupRequest(null, "Updated Description", null);

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.save(any(Group.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Group result = groupService.updateGroupById(request, groupId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Old Name"); // Should remain unchanged
            assertThat(result.getDescription()).isEqualTo("Updated Description");
            verify(groupRepository, times(1)).save(any(Group.class));
        }
    }

    @Nested
    @DisplayName("Delete Group By ID Tests")
    class DeleteGroupByIdTests {

        @Test
        @DisplayName("Should delete group successfully when group exists")
        void deleteGroupById_ShouldDeleteGroupSuccessfully_WhenGroupExists() {
            // Arrange
            Group group = Group.builder().id(groupId).name("Test Group").patient(patient).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(patientService.getPatientProfile()).thenReturn(patient);
            doNothing().when(groupRepository).delete(group);

            // Act
            groupService.deleteGroupById(groupId);

            // Assert
            verify(groupRepository, times(1)).findById(groupId);
            verify(groupRepository, times(1)).delete(group);
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group does not exist")
        void deleteGroupById_ShouldThrowGroupNotFoundException_WhenGroupDoesNotExist() {
            // Arrange
            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatExceptionOfType(GroupNotFoundException.class)
                    .isThrownBy(() -> groupService.deleteGroupById(groupId))
                    .withMessageContaining("Group not found with id: " + groupId);

            verify(groupRepository, times(1)).findById(groupId);
            verify(groupRepository, never()).delete(any(Group.class));
        }
    }

    @Nested
    @DisplayName("Remove Medicine From Group Tests")
    class RemoveMedicineFromGroupTests {

        @Test
        @DisplayName("Should remove medicine from group successfully when medicine exists in group")
        void removeFromGroup_ShouldRemoveMedicineSuccessfully_WhenMedicineExistsInGroup() {
            // Arrange
            UUID medicineId = UUID.randomUUID();
            Medicine medicine = Medicine.builder().id(medicineId).name("Aspirin")
                    .drug(Drug.builder().id(UUID.randomUUID()).build()).build();

            List<Medicine> medicines = new ArrayList<>();
            medicines.add(medicine);

            Group group = Group.builder().id(groupId).name("Pain Relief").patient(patient)
                    .medicines(medicines).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.save(any(Group.class))).thenReturn(group);

            // Act
            groupService.removeFromGroup(groupId, medicineId);

            // Assert
            assertThat(group.getMedicines().size()).isEqualTo(0);
            assertThat(medicine.getGroup()).isNull();
            verify(groupRepository, times(1)).findById(groupId);
            verify(groupRepository, times(1)).save(group);
        }

        @Test
        @DisplayName("Should throw MedicineNotFound when medicine does not exist in group")
        void removeFromGroup_ShouldThrowMedicineNotFound_WhenMedicineDoesNotExistInGroup() {
            // Arrange
            UUID medicineId = UUID.randomUUID();
            UUID differentMedicineId = UUID.randomUUID();
            Medicine medicine = Medicine.builder().id(differentMedicineId).name("Aspirin").build();

            List<Medicine> medicines = new ArrayList<>();
            medicines.add(medicine);

            Group group = Group.builder().id(groupId).name("Pain Relief").patient(patient)
                    .medicines(medicines).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(patientService.getPatientProfile()).thenReturn(patient);

            // Act & Assert
            assertThatExceptionOfType(MedicineNotFound.class)
                    .isThrownBy(() -> groupService.removeFromGroup(groupId, medicineId))
                    .withMessageContaining(
                            "Medicine with id " + medicineId + " not found in group Pain Relief");

            verify(groupRepository, times(1)).findById(groupId);
            verify(groupRepository, never()).save(any(Group.class));
        }

        @Test
        @DisplayName("Should handle empty medicine list in group")
        void removeFromGroup_ShouldThrowMedicineNotFound_WhenGroupHasNoMedicines() {
            // Arrange
            UUID medicineId = UUID.randomUUID();
            List<Medicine> medicines = new ArrayList<>();

            Group group = Group.builder().id(groupId).name("Empty Group").patient(patient)
                    .medicines(medicines).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(patientService.getPatientProfile()).thenReturn(patient);

            // Act & Assert
            assertThatExceptionOfType(MedicineNotFound.class)
                    .isThrownBy(() -> groupService.removeFromGroup(groupId, medicineId))
                    .withMessageContaining(
                            "Medicine with id " + medicineId + " not found in group Empty Group");

            verify(groupRepository, times(1)).findById(groupId);
            verify(groupRepository, never()).save(any(Group.class));
        }
    }
}
