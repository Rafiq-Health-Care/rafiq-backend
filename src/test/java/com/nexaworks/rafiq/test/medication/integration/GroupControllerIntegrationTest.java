package com.nexaworks.rafiq.test.medication.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.repository.DrugRepository;
import com.nexaworks.rafiq.medication.repository.GroupRepository;
import com.nexaworks.rafiq.medication.repository.MedicineRepository;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.medication.api.dto.request.AddGroupRequest;
import com.nexaworks.rafiq.medication.api.dto.request.AddMedicinesToGroup;
import com.nexaworks.rafiq.medication.api.dto.request.UpdateGroupRequest;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.medication.entity.enums.Color;
import com.nexaworks.rafiq.medication.entity.enums.MedicineFrequency;
import com.nexaworks.rafiq.test.BaseIntegrationTest;
import com.nexaworks.rafiq.user.entity.enums.Gender;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;

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
    @Autowired
    private PatientRepository patientRepository;

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

        // Create Patient directly (Patient extends User with is-a relationship)
        Patient patient = Patient.builder().email(email)
                .password(passwordEncoder.encode("Valid@1234")).firstName(firstName)
                .lastName(lastName).phone(phone).birthDate(LocalDate.of(1990, 1, 1)).gender(gender)
                .roles(Set.of(patientRole)).enabled(true).build();
        return patientRepository.save(patient);
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
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group added successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Pain Relief"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.description")
                            .value("Medications for pain management"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.color").value("BLUE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.groupId").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.patientId")
                            .value(user.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.updatedAt").exists())
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
                    .with(withUserId(user)))
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
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isCreated());

            // Try to add group with same name
            AddGroupRequest duplicateRequest = new AddGroupRequest("Vitamins",
                    "Different description", Color.RED);
            String duplicatePayload = objectMapper.writeValueAsString(duplicateRequest);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_GROUP_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON).content(duplicatePayload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isConflict())
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
            Group group1 = Group.builder()
                    .name("Antibiotics").description("Antibiotic medications").color(Color.RED)
                    .patientId(user.getId()).build();

            Group group2 = Group.builder()
                    .name("Vitamins").description("Daily vitamins").color(Color.GREEN)
                    .patientId(user.getId()).build();

            Group group3 = Group.builder()
                    .name("Pain Relief").description("Pain medications").color(Color.BLUE)
                    .patientId(user.getId()).build();

            groupRepository.save(group1);
            groupRepository.save(group2);
            groupRepository.save(group3);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_GROUPS_ENDPOINT).param("page", "0")
                    .param("size", "10").with(withUserId(user)))
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
                    .param("size", "10").with(withUserId(user)))
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
            Group group1 = Group.builder()
                    .name("A-Group").description("First alphabetically").color(Color.RED)
                    .patientId(user.getId()).build();

            Group group2 = Group.builder()
                    .name("C-Group").description("Third alphabetically").color(Color.GREEN)
                    .patientId(user.getId()).build();

            Group group3 = Group.builder()
                    .name("B-Group").description("Second alphabetically").color(Color.BLUE)
                    .patientId(user.getId()).build();

            groupRepository.save(group1);
            groupRepository.save(group2);
            groupRepository.save(group3);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.get(GET_ALL_GROUPS_ENDPOINT).param("page", "0")
                    .param("size", "10").param("sort", "name").param("direction", "desc")
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isOk())
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

            Group group = Group.builder()
                    .name("Heart Medications").description("Medications for heart health")
                    .color(Color.RED).patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_GROUP_BY_ID_ENDPOINT + savedGroup.getId()).with(withUserId(user)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name")
                            .value("Heart Medications"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.description")
                            .value("Medications for heart health"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.color").value("RED"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicines").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.userId")
                            .value(user.getId().toString()))
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
            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_GROUP_BY_ID_ENDPOINT + nonExistentGroupId).with(withUserId(user)))
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
            Group group = Group.builder()
                    .name("User1 Group").description("Group belonging to user1").color(Color.BLUE)
                    .patientId(user1.getId()).build();

            Group savedGroup = groupRepository.save(group);

            // Try to access with user2
            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .get(GET_GROUP_BY_ID_ENDPOINT + savedGroup.getId()).with(withUserId(user2)))
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

            Group group = Group.builder()
                    .name("Old Name").description("Old Description").color(Color.RED)
                    .patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);

            UpdateGroupRequest request = new UpdateGroupRequest("New Name", "New Description",
                    Color.BLUE);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.patch(UPDATE_GROUP_ENDPOINT + savedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group updated successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("New Name"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.description")
                            .value("New Description"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.color").value("BLUE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.patientId")
                            .value(user.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.createdAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.updatedAt").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.medicineCount").value(0));

            // Verify update in database
            Group updatedGroup = groupRepository
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
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value("Group not found with id: " + nonExistentGroupId));
        }

        @Test
        @DisplayName("Should return 409 Conflict when updating to existing group name")
        void shouldReturnConflict_WhenUpdatingToExistingGroupName() throws Exception {
            // Arrange
            User user = createTestUser();

            Group group1 = Group.builder()
                    .name("Existing Group").description("Description").color(Color.RED)
                    .patientId(user.getId()).build();

            Group group2 = Group.builder()
                    .name("Group to Update").description("Description").color(Color.BLUE)
                    .patientId(user.getId()).build();

            groupRepository.save(group1);
            Group savedGroup2 = groupRepository.save(group2);

            UpdateGroupRequest request = new UpdateGroupRequest("Existing Group", "New Description",
                    Color.GREEN);
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(
                    MockMvcRequestBuilders.patch(UPDATE_GROUP_ENDPOINT + savedGroup2.getId())
                            .contentType(MediaType.APPLICATION_JSON).content(payload)
                            .with(withUserId(user)))
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

            Group group = Group.builder()
                    .name("Group to Delete").description("Will be deleted").color(Color.RED)
                    .patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);
            assertThat(groupRepository.count()).isEqualTo(1);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_GROUP_ENDPOINT + savedGroup.getId()).with(withUserId(user)))
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
            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_GROUP_ENDPOINT + nonExistentGroupId).with(withUserId(user)))
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

            Group group = Group.builder()
                    .name("User1 Group").description("Belongs to user1").color(Color.RED)
                    .patientId(user1.getId()).build();

            Group savedGroup = groupRepository.save(group);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders
                    .delete(DELETE_GROUP_ENDPOINT + savedGroup.getId()).with(withUserId(user2)))
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

            Group group = Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);

            // Create medicines
            Drug drug1 = Drug.builder().tradeName("Aspirin").dosageForm("Tablet").build();
            Drug drug2 = Drug.builder().tradeName("Ibuprofen").dosageForm("Tablet").build();
            drugRepository.save(drug1);
            drugRepository.save(drug2);

            Medicine medicine1 = Medicine.builder().name("Aspirin").drug(drug1).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).patientId(user.getId()).build();

            Medicine medicine2 = Medicine.builder().name("Ibuprofen").drug(drug2).dosage("200mg")
                    .frequency(MedicineFrequency.TWICE).patientId(user.getId()).build();

            Medicine savedMedicine1 = medicineRepository.save(medicine1);
            Medicine savedMedicine2 = medicineRepository.save(medicine2);

            AddMedicinesToGroup request = new AddMedicinesToGroup(
                    Arrays.asList(savedMedicine1.getId(), savedMedicine2.getId()));
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINES_ENDPOINT + savedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isOk())
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
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should add only valid medicines and skip invalid ones")
        void shouldAddOnlyValidMedicines_WhenSomeAreInvalid() throws Exception {
            // Arrange
            User user = createTestUser();

            Group group = Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);

            Drug drug = Drug.builder().tradeName("Aspirin").dosageForm("Tablet").build();
            drugRepository.save(drug);

            Medicine medicine = Medicine.builder().name("Aspirin").drug(drug).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).patientId(user.getId()).build();

            Medicine savedMedicine = medicineRepository.save(medicine);

            // Include one valid and one invalid medicine ID
            AddMedicinesToGroup request = new AddMedicinesToGroup(
                    Arrays.asList(savedMedicine.getId(), UUID.randomUUID()));
            String payload = objectMapper.writeValueAsString(request);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(ADD_MEDICINES_ENDPOINT + savedGroup.getId())
                    .contentType(MediaType.APPLICATION_JSON).content(payload)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isOk())
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

            Group group = Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);

            Drug drug = Drug.builder().tradeName("Aspirin").dosageForm("Tablet").build();
            drugRepository.save(drug);

            Medicine medicine = Medicine.builder().name("Aspirin").drug(drug).dosage("100mg")
                    .frequency(MedicineFrequency.ONCE).patientId(user.getId()).group(savedGroup)
                    .build();

            Medicine savedMedicine = medicineRepository.save(medicine);

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(
                    REMOVE_MEDICINE_ENDPOINT + savedGroup.getId() + "/" + savedMedicine.getId())
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isOk())
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
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 Not Found when medicine does not exist in group")
        void shouldReturnNotFound_WhenMedicineDoesNotExistInGroup() throws Exception {
            // Arrange
            User user = createTestUser();

            Group group = Group.builder()
                    .name("Test Group").description("Group for medicines").color(Color.BLUE)
                    .patientId(user.getId()).build();

            Group savedGroup = groupRepository.save(group);
            UUID nonExistentMedicineId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(MockMvcRequestBuilders.post(
                    REMOVE_MEDICINE_ENDPOINT + savedGroup.getId() + "/" + nonExistentMedicineId)
                    .with(withUserId(user))).andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Medicine with id "
                            + nonExistentMedicineId + " not found in group Test Group"));
        }
    }
}
