package com.nexaworks.rafiq.integration.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.dto.request.group.AddGroupRequest;
import com.nexaworks.rafiq.dto.request.group.AddMedicinesToGroup;
import com.nexaworks.rafiq.dto.request.group.UpdateGroupRequest;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.Color;
import com.nexaworks.rafiq.entities.enums.Gender;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.integration.BaseIntegrationTest;
import com.nexaworks.rafiq.repository.DrugRepository;
import com.nexaworks.rafiq.repository.GroupRepository;
import com.nexaworks.rafiq.repository.MedicineRepository;
import com.nexaworks.rafiq.repository.RoleRepository;
import com.nexaworks.rafiq.repository.UserRepository;

@DisplayName("Group Controller Integration Test Cases")
public class GroupControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MedicineRepository medicineRepository;

    @Autowired
    DrugRepository drugRepository;

    @BeforeEach
    void setUp() {
        medicineRepository.deleteAll();
        groupRepository.deleteAll();
        drugRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createTestUser() {
        return createTestUser("email@test.com", "John", "Doe", "+12345678901", Gender.MALE);
    }

    private User createTestUser(String email, String firstName, String lastName, String phone,
            Gender gender) {
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        User user = User.builder().email(email).password(passwordEncoder.encode("Valid@1234"))
                .firstName(firstName).lastName(lastName).phone(phone).age(30).gender(gender)
                .roles(Set.of(patientRole)).enabled(true)
                .patientProfile(PatientProfile.builder().build()).build();
        return userRepository.save(user);
    }

    @Nested
    @DisplayName("Add Group Tests")
    class AddGroupTests {
        private final String ADD_GROUP_ENDPOINT = "/group/add";

        @Test
        @DisplayName("Should add group successfully and return 201 Created when request is valid")
        void shouldAddGroup_WhenRequestIsValid() throws Exception {
            // Arrange
            User user = createTestUser();
            AddGroupRequest request = new AddGroupRequest("Pain Relief",
                    "Medications for pain management", Color.BLUE);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_GROUP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group added successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Pain Relief"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.description")
                            .value("Medications for pain management"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.color").value("BLUE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.groupId").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.patientId")
                            .value(user.getPatientProfile().getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.updatedAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.iconUrl").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicineCount").value(0));

            // Verify group was saved in database
            assertThat(groupRepository.count()).isEqualTo(1);
            assertThat(groupRepository.findAll().get(0).getName()).isEqualTo("Pain Relief");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when group name is null")
        void shouldReturnBadRequest_WhenGroupNameIsNull() throws Exception {
            // Arrange
            User user = createTestUser();
            AddGroupRequest request = new AddGroupRequest(null, "Medications for pain management",
                    Color.BLUE);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_GROUP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            // Verify no group was saved
            assertThat(groupRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 409 Conflict when group name already exists")
        void shouldReturnConflict_WhenGroupNameAlreadyExists() throws Exception {
            // Arrange
            User user = createTestUser();
            AddGroupRequest request = new AddGroupRequest("Vitamins",
                    "Daily vitamins and supplements", Color.GREEN);
            String payload = objectMapper.writeValueAsString(request);

            // Add first group
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_GROUP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            // Try to add group with same name
            AddGroupRequest duplicateRequest = new AddGroupRequest("Vitamins",
                    "Different description", Color.RED);
            String duplicatePayload = objectMapper.writeValueAsString(duplicateRequest);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_GROUP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(duplicatePayload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group with name Vitamins already exists"));

            // Verify only one group was saved
            assertThat(groupRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Get All Groups Tests")
    class GetAllGroupsTests {
        private final String GET_ALL_GROUPS_ENDPOINT = "/group";

        @Test
        @DisplayName("Should return paginated groups with default parameters and 200 OK when user has groups")
        void shouldReturnPaginatedGroups_WhenUserHasGroups() throws Exception {
            // Arrange
            User user = createTestUser();

            // Create multiple groups for the user
            com.nexaworks.rafiq.entities.Group group1 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Antibiotics").description("Antibiotic medications").color(Color.RED)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group group2 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Vitamins").description("Daily vitamins").color(Color.GREEN)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group group3 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Pain Relief").description("Pain medications").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            groupRepository.save(group1);
            groupRepository.save(group2);
            groupRepository.save(group3);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_GROUPS_ENDPOINT).param("page", "0")
                    .param("size", "10").with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
        }

        @Test
        @DisplayName("Should return empty page with 200 OK when user has no groups")
        void shouldReturnEmptyPage_WhenUserHasNoGroups() throws Exception {
            // Arrange
            User user = createTestUser();

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_GROUPS_ENDPOINT).param("page", "0")
                    .param("size", "10").with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.firstPage").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastPage").value(true));
        }

        @Test
        @DisplayName("Should return groups sorted by name in descending order when direction is desc")
        void shouldReturnGroupsSortedDescending_WhenDirectionIsDesc() throws Exception {
            // Arrange
            User user = createTestUser();

            // Create groups with different names
            com.nexaworks.rafiq.entities.Group group1 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("A-Group").description("First alphabetically").color(Color.RED)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group group2 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("C-Group").description("Third alphabetically").color(Color.GREEN)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group group3 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("B-Group").description("Second alphabetically").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            groupRepository.save(group1);
            groupRepository.save(group2);
            groupRepository.save(group3);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_GROUPS_ENDPOINT).param("page", "0")
                    .param("size", "10").param("sort", "name").param("direction", "desc")
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("C-Group"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name").value("B-Group"))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.content[2].name").value("A-Group"));
        }
    }

    @Nested
    @DisplayName("Get Group By ID Tests")
    class GetGroupByIdTests {
        private final String GET_GROUP_BY_ID_ENDPOINT = "/group/";

        @Test
        @DisplayName("Should return group details with 200 OK when group exists and belongs to user")
        void shouldReturnGroupDetails_WhenGroupExistsAndBelongsToUser() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Heart Medications").description("Medications for heart health")
                    .color(Color.RED).patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_GROUP_BY_ID_ENDPOINT + savedGroup.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name")
                            .value("Heart Medications"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.description")
                            .value("Medications for heart health"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.color").value("RED"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicines").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId")
                            .value(user.getPatientProfile().getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.iconUrl").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicineCount").value(0))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.updatedAt").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found when group does not exist")
        void shouldReturnNotFound_WhenGroupDoesNotExist() throws Exception {
            // Arrange
            User user = createTestUser();
            java.util.UUID nonExistentGroupId = java.util.UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_GROUP_BY_ID_ENDPOINT + nonExistentGroupId)
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group not found with id: " + nonExistentGroupId));
        }

        @Test
        @DisplayName("Should return 404 Not Found when group belongs to different user")
        void shouldReturnNotFound_WhenGroupBelongsToDifferentUser() throws Exception {
            // Arrange
            User user1 = createTestUser("user1@test.com", "User", "One", "+11111111111",
                    Gender.MALE);
            User user2 = createTestUser("user2@test.com", "User", "Two", "+22222222222",
                    Gender.FEMALE);

            // Create group for user1
            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("User1 Group").description("Group belonging to user1").color(Color.BLUE)
                    .patientProfile(user1.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            // Try to access with user2
            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.get(GET_GROUP_BY_ID_ENDPOINT + savedGroup.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user2)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group not found with id: " + savedGroup.getId()));
        }
    }

    @Nested
    @DisplayName("Update Group Tests")
    class UpdateGroupTests {
        private final String UPDATE_GROUP_ENDPOINT = "/group/";

        @Test
        @DisplayName("Should update group successfully and return 200 OK when request is valid")
        void shouldUpdateGroup_WhenRequestIsValid() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Old Name").description("Old Description").color(Color.RED)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Description",
                    Color.BLUE);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_GROUP_ENDPOINT + savedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group updated successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("New Name"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.description")
                            .value("New Description"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.color").value("BLUE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.patientId")
                            .value(user.getPatientProfile().getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.updatedAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.iconUrl").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicineCount").value(0));

            // Verify update in database
            com.nexaworks.rafiq.entities.Group updatedGroup = groupRepository
                    .findById(savedGroup.getId()).get();
            assertThat(updatedGroup.getName()).isEqualTo("New Name");
            assertThat(updatedGroup.getDescription()).isEqualTo("New Description");
        }

        @Test
        @DisplayName("Should return 404 Not Found when updating non-existent group")
        void shouldReturnNotFound_WhenUpdatingNonExistentGroup() throws Exception {
            // Arrange
            User user = createTestUser();
            UUID nonExistentGroupId = UUID.randomUUID();

            UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Description",
                    Color.GREEN);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_GROUP_ENDPOINT + nonExistentGroupId)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group not found with id: " + nonExistentGroupId));
        }

        @Test
        @DisplayName("Should return 409 Conflict when updating to existing group name")
        void shouldReturnConflict_WhenUpdatingToExistingGroupName() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group1 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Existing Group").description("Description").color(Color.RED)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group group2 = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Group to Update").description("Description").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            groupRepository.save(group1);
            com.nexaworks.rafiq.entities.Group savedGroup2 = groupRepository.save(group2);

            UpdateGroupRequest request = new UpdateGroupRequest("Existing Group", "New Description",
                    Color.GREEN);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.patch(UPDATE_GROUP_ENDPOINT + savedGroup2.getId())
                            .contentType(MediaType.APPLICATION_JSON).content(payload)
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group with name Existing Group already exists"));
        }
    }

    @Nested
    @DisplayName("Delete Group Tests")
    class DeleteGroupTests {
        private final String DELETE_GROUP_ENDPOINT = "/group/";

        @Test
        @DisplayName("Should delete group successfully and return 204 No Content when group exists")
        void shouldDeleteGroup_WhenGroupExists() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Group to Delete").description("Will be deleted").color(Color.RED)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);
            assertThat(groupRepository.count()).isEqualTo(1);

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_GROUP_ENDPOINT + savedGroup.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNoContent());

            // Verify deletion
            assertThat(groupRepository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting non-existent group")
        void shouldReturnNotFound_WhenDeletingNonExistentGroup() throws Exception {
            // Arrange
            User user = createTestUser();
            UUID nonExistentGroupId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_GROUP_ENDPOINT + nonExistentGroupId)
                            .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group not found with id: " + nonExistentGroupId));
        }

        @Test
        @DisplayName("Should return 404 Not Found when deleting group of different user")
        void shouldReturnNotFound_WhenDeletingGroupOfDifferentUser() throws Exception {
            // Arrange
            User user1 = createTestUser("user1@test.com", "User", "One", "+11111111111",
                    Gender.MALE);
            User user2 = createTestUser("user2@test.com", "User", "Two", "+22222222222",
                    Gender.FEMALE);

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("User1 Group").description("Belongs to user1").color(Color.RED)
                    .patientProfile(user1.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.delete(DELETE_GROUP_ENDPOINT + savedGroup.getId())
                            .with(SecurityMockMvcRequestPostProcessors.user(user2)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            // Verify group was not deleted
            assertThat(groupRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Add Medicines to Group Tests")
    class AddMedicinesToGroupTests {
        private final String ADD_MEDICINES_ENDPOINT = "/group/addMedicines/";

        @Test
        @DisplayName("Should add medicines to group successfully and return 200 OK")
        void shouldAddMedicinesToGroup_WhenRequestIsValid() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            // Create medicines
            Drug drug1 = Drug.builder().tradeName("Aspirin").dosageForm("Tablet").build();
            Drug drug2 = Drug.builder().tradeName("Ibuprofen").dosageForm("Tablet").build();
            drugRepository.save(drug1);
            drugRepository.save(drug2);

            Medicine medicine1 = Medicine.builder().name("Aspirin").drug(drug1).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).patient(user.getPatientProfile()).build();

            Medicine medicine2 = Medicine.builder().name("Ibuprofen").drug(drug2).dosage("200mg")
                    .frequency(MedicineFrequency.TWICE).patient(user.getPatientProfile()).build();

            Medicine savedMedicine1 = medicineRepository.save(medicine1);
            Medicine savedMedicine2 = medicineRepository.save(medicine2);

            AddMedicinesToGroup request = new AddMedicinesToGroup(
                    Arrays.asList(savedMedicine1.getId(), savedMedicine2.getId()));
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINES_ENDPOINT + savedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Medicines added to group successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.groupId")
                            .value(savedGroup.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.addedCount").value(2));
        }

        @Test
        @DisplayName("Should return 404 Not Found when group does not exist")
        void shouldReturnNotFound_WhenGroupDoesNotExist() throws Exception {
            // Arrange
            User user = createTestUser();
            UUID nonExistentGroupId = UUID.randomUUID();

            AddMedicinesToGroup request = new AddMedicinesToGroup(
                    Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINES_ENDPOINT + nonExistentGroupId)
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should add only valid medicines and skip invalid ones")
        void shouldAddOnlyValidMedicines_WhenSomeAreInvalid() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            Drug drug = Drug.builder().tradeName("Aspirin").dosageForm("Tablet").build();
            drugRepository.save(drug);

            Medicine medicine = Medicine.builder().name("Aspirin").drug(drug).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).patient(user.getPatientProfile()).build();

            Medicine savedMedicine = medicineRepository.save(medicine);

            // Include one valid and one invalid medicine ID
            AddMedicinesToGroup request = new AddMedicinesToGroup(
                    Arrays.asList(savedMedicine.getId(), UUID.randomUUID()));
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINES_ENDPOINT + savedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.addedCount").value(1));
        }
    }

    @Nested
    @DisplayName("Remove Medicine from Group Tests")
    class RemoveMedicineFromGroupTests {
        private final String REMOVE_MEDICINE_ENDPOINT = "/group/removeMedicines/";

        @Test
        @DisplayName("Should remove medicine from group successfully and return 200 OK")
        void shouldRemoveMedicineFromGroup_WhenMedicineExistsInGroup() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);

            Drug drug = Drug.builder().tradeName("Aspirin").dosageForm("Tablet").build();
            drugRepository.save(drug);

            Medicine medicine = Medicine.builder().name("Aspirin").drug(drug).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).patient(user.getPatientProfile())
                    .group(savedGroup).build();

            Medicine savedMedicine = medicineRepository.save(medicine);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .post(REMOVE_MEDICINE_ENDPOINT + savedGroup.getId() + "/"
                            + savedMedicine.getId())
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().json(
                            "{\"success\":true,\"message\":\"Medicine removed from group\"}"));

            // Verify medicine was removed from group
            Medicine updatedMedicine = medicineRepository.findById(savedMedicine.getId()).get();
            assertThat(updatedMedicine.getGroup()).isNull();
        }

        @Test
        @DisplayName("Should return 404 Not Found when group does not exist")
        void shouldReturnNotFound_WhenGroupDoesNotExist() throws Exception {
            // Arrange
            User user = createTestUser();
            UUID nonExistentGroupId = UUID.randomUUID();
            UUID medicineId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .post(REMOVE_MEDICINE_ENDPOINT + nonExistentGroupId + "/" + medicineId)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine does not exist in group")
        void shouldReturnNotFound_WhenMedicineDoesNotExistInGroup() throws Exception {
            // Arrange
            User user = createTestUser();

            com.nexaworks.rafiq.entities.Group group = com.nexaworks.rafiq.entities.Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientProfile(user.getPatientProfile()).build();

            com.nexaworks.rafiq.entities.Group savedGroup = groupRepository.save(group);
            UUID nonExistentMedicineId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .post(REMOVE_MEDICINE_ENDPOINT + savedGroup.getId() + "/"
                            + nonExistentMedicineId)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Medicine with id "
                            + nonExistentMedicineId + " not found in group Test Group"));
        }
    }
}
