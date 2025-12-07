package com.nexaworks.rafiq.test.fileManagment.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.nexaworks.rafiq.ai.service.AiService;
import com.nexaworks.rafiq.fileManagment.repository.FileMetaDataRepository;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import com.nexaworks.rafiq.test.BaseIntegrationTest;
import com.nexaworks.rafiq.user.entity.enums.Gender;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.repository.RoleRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;

@DisplayName("File Controller Integration Test")
class FileControllerIntegrationTest extends BaseIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AiService mockAiService() {
            // Return a mock implementation that returns predictable results
            return pdfFile -> {
                // Return a default mock response
                return "{\"name\":\"Test Result\",\"date\":\"2024-01-15\",\"tests\":[]}";
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FileMetaDataRepository fileMetaDataRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up in correct order
        fileMetaDataRepository.deleteAll();
        userRepository.deleteAll();
        patientRepository.deleteAll();

        // Create test user with patient profile
        testUser = createTestPatient("patient@example.com", "TestPass@123", "John", "Doe");
    }

    private User createTestPatient(String email, String password, String firstName,
            String lastName) {
        // Get or create PATIENT role
        Role patientRole = roleRepository.findByName("ROLE_PATIENT");
        if (patientRole == null) {
            patientRole = new Role();
            patientRole.setName("ROLE_PATIENT");
            patientRole = roleRepository.save(patientRole);
        }

        // Create User
        User user = User.builder().email(email).password(passwordEncoder.encode(password))
                .firstName(firstName).lastName(lastName).phone("+12345678901")
                .birthDate(LocalDate.of(1990, 1, 1)).gender(Gender.MALE).roles(Set.of(patientRole))
                .enabled(true).build();
        user = userRepository.save(user);

        // Create Patient
        Patient patient = Patient.builder().id(user.getId()).email(email).firstName(firstName)
                .lastName(lastName).phone("+12345678901").description("Test patient").build();
        patientRepository.save(patient);

        return user;
    }

    @Nested
    @DisplayName("Upload File")
    class UploadFile {
        private final String UPLOAD_ENDPOINT = "/file/upload";

        @Nested
        @DisplayName("Should Upload Successfully")
        class ShouldUploadSuccessfully {

            @Test
            @DisplayName("Should upload PDF and return extracted results with file ID")
            void shouldUploadPdfAndReturnExtractedResultsWithFileId() throws Exception {
                // Arrange - Create a mock PDF file
                MockMultipartFile pdfFile = new MockMultipartFile("file", "test-lab-result.pdf",
                        "application/pdf", "PDF content here".getBytes());

                // Act & Assert - Upload PDF with authentication
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(pdfFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.content()
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Result"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.date").value("2024-01-15"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.tests").isArray())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.fileId").exists());

                // Verify file metadata was saved in database
                long fileCount = fileMetaDataRepository.count();
                assertThat(fileCount).isEqualTo(1)
                        .as("File metadata should be saved in database after upload");
            }

            @Test
            @DisplayName("Should upload image file and convert to PDF then extract results")
            void shouldUploadImageAndConvertToPdfThenExtractResults() throws Exception {
                // Arrange - Create a mock image file
                MockMultipartFile imageFile = new MockMultipartFile("file", "lab-result.jpg",
                        "image/jpeg", createMinimalPngImage());

                // Act & Assert - Upload image with authentication
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(imageFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.content()
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Result"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.fileId").exists());

                // Verify file was saved
                assertThat(fileMetaDataRepository.count()).isEqualTo(1);
            }

            @Test
            @DisplayName("Should upload PNG image file successfully")
            void shouldUploadPngImageFileSuccessfully() throws Exception {
                // Arrange
                MockMultipartFile pngFile = new MockMultipartFile("file", "lab-result.png",
                        "image/png", createMinimalPngImage());

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(pngFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.content()
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.fileId").exists());

                assertThat(fileMetaDataRepository.count()).isEqualTo(1);
            }

            private byte[] createMinimalPngImage() {
                return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00,
                        0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00,
                        0x00, 0x01, 0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
                        (byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, 0x78,
                        (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A,
                        0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
                        (byte) 0xAE, 0x42, 0x60, (byte) 0x82};
            }
        }

        @Nested
        @DisplayName("Should Fail Upload")
        class ShouldFailUpload {

            @Test
            @DisplayName("Should return 401 Unauthorized when user is not authenticated")
            void shouldReturnUnauthorizedWhenUserNotAuthenticated() throws Exception {
                // Arrange - Create a mock PDF file
                MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf",
                        "application/pdf", "PDF content".getBytes());

                // Act & Assert - Upload without authentication
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(pdfFile))
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized());

                // Verify no file was saved
                assertThat(fileMetaDataRepository.count()).isZero()
                        .as("No file should be saved when authentication fails");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when file is empty")
            void shouldReturnBadRequestWhenFileIsEmpty() throws Exception {
                // Arrange - Create an empty file
                MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf",
                        "application/pdf", new byte[0]);

                // Act & Assert - Upload empty file with authentication
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(emptyFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no file was saved
                assertThat(fileMetaDataRepository.count()).isZero()
                        .as("No file should be saved for empty file");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when file parameter is missing")
            void shouldReturnBadRequestWhenFileParameterMissing() throws Exception {
                // Act & Assert - Upload without file parameter
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no file was saved
                assertThat(fileMetaDataRepository.count()).isZero();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when file type is unsupported")
            void shouldReturnBadRequestWhenFileTypeIsUnsupported() throws Exception {
                // Arrange - Create a text file (unsupported type)
                MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain",
                        "Text content".getBytes());

                // Act & Assert - Upload unsupported file type
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(textFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                // Verify no file was saved
                assertThat(fileMetaDataRepository.count()).isZero()
                        .as("No file should be saved for unsupported file type");
            }

            @Test
            @DisplayName("Should return 400 Bad Request when uploading Word document")
            void shouldReturnBadRequestWhenUploadingWordDocument() throws Exception {
                // Arrange - Create a Word document (unsupported type)
                MockMultipartFile wordFile = new MockMultipartFile("file", "test.docx",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "Word content".getBytes());

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(wordFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(fileMetaDataRepository.count()).isZero();
            }

            @Test
            @DisplayName("Should return 400 Bad Request when uploading Excel file")
            void shouldReturnBadRequestWhenUploadingExcelFile() throws Exception {
                // Arrange - Create an Excel file (unsupported type)
                MockMultipartFile excelFile = new MockMultipartFile("file", "test.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "Excel content".getBytes());

                // Act & Assert
                mockMvc.perform(MockMvcRequestBuilders.multipart(UPLOAD_ENDPOINT).file(excelFile)
                        .with(withUserId(testUser)))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest());

                assertThat(fileMetaDataRepository.count()).isZero();
            }
        }
    }
}
