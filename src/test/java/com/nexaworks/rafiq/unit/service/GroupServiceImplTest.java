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

import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.dto.response.Group.AddGroupResponse;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Group;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.exception.custom.medicine.GroupIsAlreadyExistsException;
import com.nexaworks.rafiq.exception.custom.medicine.GroupNotFoundException;
import com.nexaworks.rafiq.exception.custom.medicine.MedicineNotFound;
import com.nexaworks.rafiq.mapper.GroupMapper;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.repository.GroupRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.medicine.GroupServiceImpl;
import com.nexaworks.rafiq.service.patient.PatientService;

@DisplayName("GroupService Unit Tests")
public class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private AuthService authService;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private MedicineMapper medicineMapper;

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
            AddGroupRequest request = new AddGroupRequest("Vitamins", "Vitamin supplements",
                    "#0000FF");
            Group group = Group.builder().name("Vitamins").description("Vitamin supplements")
                    .color("#0000FF").build();

            Group savedGroup = Group.builder().id(groupId).name("Vitamins")
                    .description("Vitamin supplements").color("#0000FF").patient(patient).build();
            AddGroupResponse response = new AddGroupResponse(groupId, patientId,
                    "Vitamin supplements", "#0000FF", "Vitamins", null, null, null, 0);

            when(authService.getAuthenticateUser()).thenReturn(patient);
            when(groupMapper.toEntity(request)).thenReturn(group);
            when(groupRepository.existsGroupByName_AndPatient("Vitamins", patient))
                    .thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);
            when(groupMapper.toDto(savedGroup)).thenReturn(response);

            // Act
            AddGroupResponse result = groupService.addGroup(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.groupId()).isEqualTo(groupId);
            assertThat(result.name()).isEqualTo("Vitamins");
            verify(authService, times(1)).getAuthenticateUser();
            verify(groupRepository, times(1)).existsGroupByName_AndPatient("Vitamins", patient);
            verify(groupRepository, times(1)).save(any(Group.class));
        }

        @Test
        @DisplayName("Should throw GroupIsAlreadyExistsException when group name already exists")
        void addGroup_ShouldThrowGroupIsAlreadyExistsException_WhenGroupNameAlreadyExists() {
            // Arrange
            AddGroupRequest request = new AddGroupRequest("Vitamins", "Vitamin supplements", null);
            Group group = Group.builder().name("Vitamins").description("Vitamin supplements")
                    .build();

            when(authService.getAuthenticateUser()).thenReturn(patient);
            when(groupMapper.toEntity(request)).thenReturn(group);
            when(groupRepository.existsGroupByName_AndPatient("Vitamins", patient))
                    .thenReturn(true);

            // Act & Assert
            assertThatExceptionOfType(GroupIsAlreadyExistsException.class)
                    .isThrownBy(() -> groupService.addGroup(request))
                    .withMessageContaining("Group with name Vitamins already exists");

            verify(authService, times(1)).getAuthenticateUser();
            verify(groupRepository, times(1)).existsGroupByName_AndPatient("Vitamins", patient);
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

            when(authService.getAuthenticateUserId()).thenReturn(patientId);
            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);
            when(groupMapper.toDto(any(Group.class))).thenAnswer(invocation -> {
                Group g = invocation.getArgument(0);
                return new AddGroupResponse(g.getId(), patientId, null, null, g.getName(), null,
                        null, null, 0);
            });

            // Act
            PageResponse<AddGroupResponse> result = groupService.getGroups(0, 10, "asc", "name");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.numberOfElements()).isEqualTo(2);
            assertThat(result.content().size()).isEqualTo(2);
            verify(authService, times(1)).getAuthenticateUserId();
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

            when(authService.getAuthenticateUserId()).thenReturn(patientId);
            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);
            when(groupMapper.toDto(any(Group.class))).thenAnswer(invocation -> {
                Group g = invocation.getArgument(0);
                return new AddGroupResponse(g.getId(), patientId, null, null, g.getName(), null,
                        null, null, 0);
            });

            // Act
            PageResponse<AddGroupResponse> result = groupService.getGroups(0, 10, "desc", "name");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.numberOfElements()).isEqualTo(2);
            assertThat(result.content().size()).isEqualTo(2);
            verify(authService, times(1)).getAuthenticateUserId();
            verify(groupRepository, times(1)).findByPatientId(eq(patientId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty page when patient has no groups")
        void getGroups_ShouldReturnEmptyPage_WhenPatientHasNoGroups() {
            // Arrange
            List<Group> groups = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Group> groupPage = new PageImpl<>(groups, pageable, 0);

            when(authService.getAuthenticateUserId()).thenReturn(patientId);
            when(groupRepository.findByPatientId(eq(patientId), any(Pageable.class)))
                    .thenReturn(groupPage);

            // Act
            PageResponse<AddGroupResponse> result = groupService.getGroups(0, 10, "asc", "name");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.numberOfElements()).isEqualTo(0);
            assertThat(result.content().size()).isEqualTo(0);
            verify(authService, times(1)).getAuthenticateUserId();
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
                    .description("Old Description").color("#0000FF").patient(patient).build();

            UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Description",
                    "#FF0000");

            Group updatedGroup = Group.builder().id(groupId).name("New Name")
                    .description("New Description").color("#FF0000").patient(patient).build();

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.existsGroupByPatient_IdAndName(patientId, "New Name"))
                    .thenReturn(false);
            when(groupRepository.save(any(Group.class))).thenReturn(updatedGroup);
            when(groupMapper.toDto(updatedGroup)).thenReturn(new AddGroupResponse(groupId,
                    patientId, "New Description", "#0000FF", "New Name", null, null, null, 0));

            // Act
            AddGroupResponse result = groupService.updateGroupById(request, groupId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("New Name");
            assertThat(result.description()).isEqualTo("New Description");
            assertThat(result.color()).isEqualTo("#0000FF");
            verify(groupRepository, times(1)).save(any(Group.class));
        }

        @Test
        @DisplayName("Should throw GroupIsAlreadyExistsException when updating to existing group name")
        void updateGroupById_ShouldThrowGroupIsAlreadyExistsException_WhenUpdatingToExistingName() {
            // Arrange
            Group existingGroup = Group.builder().id(groupId).name("Old Name")
                    .description("Old Description").patient(patient).build();

            UpdateGroupRequest request = new UpdateGroupRequest("Existing Name", "New Description",
                    "#FF0000");

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
                    .description("Old Description").color("#0000FF").patient(patient).build();

            UpdateGroupRequest request = new UpdateGroupRequest(null, "Updated Description", null);

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
            when(patientService.getPatientProfile()).thenReturn(patient);
            when(groupRepository.save(any(Group.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(groupMapper.toDto(any(Group.class))).thenAnswer(invocation -> {
                Group g = invocation.getArgument(0);
                return new AddGroupResponse(g.getId(), patientId, g.getDescription(), g.getColor(),
                        g.getName(), null, null, null, 0);
            });

            // Act
            AddGroupResponse result = groupService.updateGroupById(request, groupId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Old Name"); // Should remain unchanged
            assertThat(result.description()).isEqualTo("Updated Description");
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
