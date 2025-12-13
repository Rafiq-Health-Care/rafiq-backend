package com.nexaworks.rafiq.test.medication.unit;

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

import com.nexaworks.rafiq.medication.api.dto.request.UpdateGroupRequest;
import com.nexaworks.rafiq.medication.entity.enums.Color;
import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.exception.GroupIsAlreadyExistsException;
import com.nexaworks.rafiq.medication.exception.GroupNotFoundException;
import com.nexaworks.rafiq.medication.exception.MedicineNotFound;
import com.nexaworks.rafiq.medication.repository.GroupRepository;
import com.nexaworks.rafiq.medication.service.implementation.GroupServiceImpl;
import com.nexaworks.rafiq.patient.service.PatientService;

@DisplayName("GroupService Unit Tests")
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private GroupServiceImpl groupService;

    private UUID patientId;
    private UUID groupId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        groupId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Get Group By ID Tests")
    class GetGroupByIdTests {

        @Test
        @DisplayName("Should return group successfully when group exists and belongs to patient")
        void getGroupById_ShouldReturnGroup_WhenGroupExistsAndBelongsToPatient() {
            // Arrange
            Group group = Group.builder().id(groupId).name("Pain Relief")
                    .description("Pain medications").patientId(patientId).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

            // Act
            Group result = groupService.getGroupById(groupId, patientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(groupId);
            assertThat(result.getName()).isEqualTo("Pain Relief");
            verify(groupRepository, times(1)).findById(groupId);
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group does not exist")
        void getGroupById_ShouldThrowGroupNotFoundException_WhenGroupDoesNotExist() {
            // Arrange
            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatExceptionOfType(GroupNotFoundException.class)
                    .isThrownBy(() -> groupService.getGroupById(groupId, patientId))
                    .withMessageContaining("Group not found with id: " + groupId);

            verify(groupRepository, times(1)).findById(groupId);
        }

        @Test
        @DisplayName("Should throw GroupNotFoundException when group belongs to different patient")
        void getGroupById_ShouldThrowGroupNotFoundException_WhenGroupBelongsToDifferentPatient() {
            // Arrange
            UUID differentPatientId = UUID.randomUUID();

            Group group = Group.builder().id(groupId).name("Pain Relief")
                    .patientId(differentPatientId).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

            // Act & Assert
            assertThatExceptionOfType(GroupNotFoundException.class)
                    .isThrownBy(() -> groupService.getGroupById(groupId, patientId))
                    .withMessageContaining("Group not found with id: " + groupId);

            verify(groupRepository, times(1)).findById(groupId);
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
                    .description("Vitamin supplements").color(Color.BLUE).patientId(patientId)
                    .build();

            when(groupRepository.existsGroupByPatientIdAndName(patientId, "Vitamins"))
                    .thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

            // Act
            Group result = groupService.addGroup(group, patientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(groupId);
            assertThat(result.getName()).isEqualTo("Vitamins");
            assertThat(result.getPatientId()).isEqualTo(patientId);
            verify(groupRepository, times(1)).existsGroupByPatientIdAndName(patientId, "Vitamins");
            verify(groupRepository, times(1)).save(any(Group.class));
        }

        @Test
        @DisplayName("Should throw GroupIsAlreadyExistsException when group name already exists")
        void addGroup_ShouldThrowGroupIsAlreadyExistsException_WhenGroupNameAlreadyExists() {
            // Arrange
            Group group = Group.builder().name("Vitamins").description("Vitamin supplements")
                    .build();

            when(groupRepository.existsGroupByPatientIdAndName(patientId, "Vitamins"))
                    .thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(GroupIsAlreadyExistsException.class)
                    .isThrownBy(() -> groupService.addGroup(group, patientId))
                    .withMessageContaining("Group with name Vitamins already exists");

            verify(groupRepository, times(1)).existsGroupByPatientIdAndName(patientId, "Vitamins");
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
            groups.add(Group.builder().id(UUID.randomUUID()).name("Group A").patientId(patientId)
                    .build());
            groups.add(Group.builder().id(UUID.randomUUID()).name("Group B").patientId(patientId)
                    .build());

            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, groups.size());

            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            Page<Group> result = groupService.getGroups(0, 10, "asc", "name", patientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().size()).isEqualTo(2);
            verify(groupRepository, times(1)).findByPatientId(eq(patientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paginated groups with descending sort")
        void getGroups_ShouldReturnPaginatedGroups_WithDescendingSort() {
            // Arrange
            List<Group> groups = new ArrayList<>();
            groups.add(Group.builder().id(UUID.randomUUID()).name("Group B").patientId(patientId)
                    .build());
            groups.add(Group.builder().id(UUID.randomUUID()).name("Group A").patientId(patientId)
                    .build());

            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, groups.size());

            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            Page<Group> result = groupService.getGroups(0, 10, "desc", "name", patientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().size()).isEqualTo(2);
            verify(groupRepository, times(1)).findByPatientId(eq(patientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when patient has no groups")
        void getGroups_ShouldReturnEmptyPage_WhenPatientHasNoGroups() {
            // Arrange
            List<Group> groups = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, 0);

            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            Page<Group> result = groupService.getGroups(0, 10, "asc", "name", patientId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent().size()).isEqualTo(0);
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
                    .description("Old Description").color(Color.BLUE).patientId(patientId).build();

            UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Description",
                    Color.RED);

            Group updatedGroup = Group.builder().id(groupId).name("New Name")
                    .description("New Description").color(Color.RED).patientId(patientId).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(groupRepository.existsGroupByPatientIdAndName(patientId, "New Name"))
                    .thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenReturn(updatedGroup);

            // Act
            Group result = groupService.updateGroupById(request, groupId, patientId);

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
                    .description("Old Description").patientId(patientId).build();

            UpdateGroupRequest request = new UpdateGroupRequest("Existing Name", "New Description",
                    Color.RED);

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(groupRepository.existsGroupByPatientIdAndName(patientId, "Existing Name"))
                    .thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(GroupIsAlreadyExistsException.class)
                    .isThrownBy(() -> groupService.updateGroupById(request, groupId, patientId))
                    .withMessageContaining("Group with name Existing Name already exists");

            verify(groupRepository, never()).save(any(Group.class));
        }

        @Test
        @DisplayName("Should update only provided fields when partial update request")
        void updateGroupById_ShouldUpdateOnlyProvidedFields_WhenPartialUpdateRequest() {
            // Arrange
            Group existingGroup = Group.builder().id(groupId).name("Old Name")
                    .description("Old Description").color(Color.BLUE).patientId(patientId).build();

            UpdateGroupRequest request = new UpdateGroupRequest(null, "Updated Description", null);

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(groupRepository.save(any(Group.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            Group result = groupService.updateGroupById(request, groupId, patientId);

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
            Group group = Group.builder().id(groupId).name("Test Group").patientId(patientId)
                    .build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            doNothing().when(groupRepository).delete(group);

            // Act
            groupService.deleteGroupById(groupId, patientId);

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
                    .isThrownBy(() -> groupService.deleteGroupById(groupId, patientId))
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

            Group group = Group.builder().id(groupId).name("Pain Relief").patientId(patientId)
                    .medicines(medicines).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(groupRepository.save(any(Group.class))).thenReturn(group);

            // Act
            groupService.removeFromGroup(groupId, medicineId, patientId);

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

            Group group = Group.builder().id(groupId).name("Pain Relief").patientId(patientId)
                    .medicines(medicines).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

            // Act & Assert
            assertThatExceptionOfType(MedicineNotFound.class)
                    .isThrownBy(() -> groupService.removeFromGroup(groupId, medicineId, patientId))
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

            Group group = Group.builder().id(groupId).name("Empty Group").patientId(patientId)
                    .medicines(medicines).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

            // Act & Assert
            assertThatExceptionOfType(MedicineNotFound.class)
                    .isThrownBy(() -> groupService.removeFromGroup(groupId, medicineId, patientId))
                    .withMessageContaining(
                            "Medicine with id " + medicineId + " not found in group Empty Group");

            verify(groupRepository, times(1)).findById(groupId);
            verify(groupRepository, never()).save(any(Group.class));
        }
    }
}
