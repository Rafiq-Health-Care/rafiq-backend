package com.nexaworks.rafiq.test.fileManagment.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.fileManagment.api.dto.UploadResults;
import com.nexaworks.rafiq.fileManagment.entity.FileMetaData;
import com.nexaworks.rafiq.fileManagment.entity.UploadType;
import com.nexaworks.rafiq.fileManagment.exception.FileException;
import com.nexaworks.rafiq.fileManagment.repository.FileMetaDataRepository;
import com.nexaworks.rafiq.fileManagment.service.implementation.CloudinaryService;
import com.nexaworks.rafiq.fileManagment.service.implementation.FileMetaDataServiceImpl;
import com.nexaworks.rafiq.shared.entity.FileCategory;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileMetaDataService Unit Tests")
class FileMetaDataServiceImplTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private FileMetaDataRepository fileMetaDataRepository;

    @InjectMocks
    private FileMetaDataServiceImpl fileMetaDataService;

    private UUID testOwnerId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testOwnerId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Save File Tests")
    class SaveFileTests {

        @Test
        @DisplayName("Should save PDF file successfully")
        void shouldSavePdfFileSuccessfully() {
            // Arrange
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    "PDF content".getBytes());

            UploadResults uploadResults = new UploadResults("cloudinary-public-id",
                    "https://cloudinary.com/test.pdf");

            UUID expectedFileId = UUID.randomUUID();
            FileMetaData savedFileMetaData = FileMetaData.builder().id(expectedFileId)
                    .originalFileName("test.pdf").cloudinaryPublicId("cloudinary-public-id")
                    .cloudinarySecureUrl("https://cloudinary.com/test.pdf").fileSize(11L)
                    .mimeType("application/pdf").category(FileCategory.LAB_TEST)
                    .ownerId(testOwnerId).userId(testUserId).build();

            when(cloudinaryService.uploadResource(any(MultipartFile.class), eq(UploadType.PDF)))
                    .thenReturn(uploadResults);
            when(fileMetaDataRepository.save(any(FileMetaData.class)))
                    .thenReturn(savedFileMetaData);

            // Act
            UUID result = fileMetaDataService.saveFile(pdfFile, testOwnerId, FileCategory.LAB_TEST,
                    testUserId);

            // Assert
            assertThat(result).isEqualTo(expectedFileId);

            ArgumentCaptor<FileMetaData> fileMetaDataCaptor = ArgumentCaptor
                    .forClass(FileMetaData.class);
            verify(fileMetaDataRepository).save(fileMetaDataCaptor.capture());

            FileMetaData capturedFileMetaData = fileMetaDataCaptor.getValue();
            assertThat(capturedFileMetaData.getOriginalFileName()).isEqualTo("test.pdf");
            assertThat(capturedFileMetaData.getCloudinaryPublicId())
                    .isEqualTo("cloudinary-public-id");
            assertThat(capturedFileMetaData.getCloudinarySecureUrl())
                    .isEqualTo("https://cloudinary.com/test.pdf");
            assertThat(capturedFileMetaData.getFileSize()).isEqualTo(11L);
            assertThat(capturedFileMetaData.getMimeType()).isEqualTo("application/pdf");
            assertThat(capturedFileMetaData.getCategory()).isEqualTo(FileCategory.LAB_TEST);
            assertThat(capturedFileMetaData.getOwnerId()).isEqualTo(testOwnerId);
            assertThat(capturedFileMetaData.getUserId()).isEqualTo(testUserId);

            verify(cloudinaryService).uploadResource(pdfFile, UploadType.PDF);
        }

        @Test
        @DisplayName("Should save image file successfully")
        void shouldSaveImageFileSuccessfully() {
            // Arrange
            MockMultipartFile imageFile = new MockMultipartFile("file", "test.jpg", "image/jpeg",
                    "Image content".getBytes());

            UploadResults uploadResults = new UploadResults("cloudinary-image-id",
                    "https://cloudinary.com/test.jpg");

            UUID expectedFileId = UUID.randomUUID();
            FileMetaData savedFileMetaData = FileMetaData.builder().id(expectedFileId).build();

            when(cloudinaryService.uploadResource(any(MultipartFile.class), eq(UploadType.IMAGE)))
                    .thenReturn(uploadResults);
            when(fileMetaDataRepository.save(any(FileMetaData.class)))
                    .thenReturn(savedFileMetaData);

            // Act
            UUID result = fileMetaDataService.saveFile(imageFile, testOwnerId,
                    FileCategory.LAB_TEST, testUserId);

            // Assert
            assertThat(result).isEqualTo(expectedFileId);

            ArgumentCaptor<FileMetaData> fileMetaDataCaptor = ArgumentCaptor
                    .forClass(FileMetaData.class);
            verify(fileMetaDataRepository).save(fileMetaDataCaptor.capture());

            FileMetaData capturedFileMetaData = fileMetaDataCaptor.getValue();
            assertThat(capturedFileMetaData.getOriginalFileName()).isEqualTo("test.jpg");
            assertThat(capturedFileMetaData.getMimeType()).isEqualTo("image/jpeg");

            verify(cloudinaryService).uploadResource(imageFile, UploadType.IMAGE);
        }

        @Test
        @DisplayName("Should save PNG image file successfully")
        void shouldSavePngImageFileSuccessfully() {
            // Arrange
            MockMultipartFile pngFile = new MockMultipartFile("file", "test.png", "image/png",
                    "PNG content".getBytes());

            UploadResults uploadResults = new UploadResults("cloudinary-png-id",
                    "https://cloudinary.com/test.png");

            UUID expectedFileId = UUID.randomUUID();
            FileMetaData savedFileMetaData = FileMetaData.builder().id(expectedFileId).build();

            when(cloudinaryService.uploadResource(any(MultipartFile.class), eq(UploadType.IMAGE)))
                    .thenReturn(uploadResults);
            when(fileMetaDataRepository.save(any(FileMetaData.class)))
                    .thenReturn(savedFileMetaData);

            // Act
            UUID result = fileMetaDataService.saveFile(pngFile, testOwnerId, FileCategory.LAB_TEST,
                    testUserId);

            // Assert
            assertThat(result).isEqualTo(expectedFileId);
            verify(cloudinaryService).uploadResource(pngFile, UploadType.IMAGE);
        }

        @Test
        @DisplayName("Should save file with null ownerId")
        void shouldSaveFileWithNullOwnerId() {
            // Arrange
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    "PDF content".getBytes());

            UploadResults uploadResults = new UploadResults("cloudinary-public-id",
                    "https://cloudinary.com/test.pdf");

            UUID expectedFileId = UUID.randomUUID();
            FileMetaData savedFileMetaData = FileMetaData.builder().id(expectedFileId).build();

            when(cloudinaryService.uploadResource(any(MultipartFile.class), eq(UploadType.PDF)))
                    .thenReturn(uploadResults);
            when(fileMetaDataRepository.save(any(FileMetaData.class)))
                    .thenReturn(savedFileMetaData);

            // Act
            UUID result = fileMetaDataService.saveFile(pdfFile, null, FileCategory.LAB_TEST,
                    testUserId);

            // Assert
            assertThat(result).isEqualTo(expectedFileId);

            ArgumentCaptor<FileMetaData> fileMetaDataCaptor = ArgumentCaptor
                    .forClass(FileMetaData.class);
            verify(fileMetaDataRepository).save(fileMetaDataCaptor.capture());

            FileMetaData capturedFileMetaData = fileMetaDataCaptor.getValue();
            assertThat(capturedFileMetaData.getOwnerId()).isNull();
        }

        @Test
        @DisplayName("Should throw FileException when file type is unsupported")
        void shouldThrowFileExceptionWhenFileTypeIsUnsupported() {
            // Arrange
            MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain",
                    "Text content".getBytes());

            // Act & Assert
            assertThatThrownBy(() -> fileMetaDataService.saveFile(textFile, testOwnerId,
                    FileCategory.LAB_TEST, testUserId)).isInstanceOf(FileException.class)
                    .hasMessageContaining("Unsupported fileMetaData type");
        }

        @Test
        @DisplayName("Should throw FileException when file content type is null")
        void shouldThrowFileExceptionWhenFileContentTypeIsNull() {
            // Arrange
            MockMultipartFile fileWithoutType = new MockMultipartFile("file", "test.dat", null,
                    "Content".getBytes());

            // Act & Assert
            assertThatThrownBy(() -> fileMetaDataService.saveFile(fileWithoutType, testOwnerId,
                    FileCategory.LAB_TEST, testUserId)).isInstanceOf(FileException.class)
                    .hasMessageContaining("Unsupported fileMetaData type");
        }
    }

    @Nested
    @DisplayName("Update File Owner Tests")
    class UpdateFileOwnerTests {

        @Test
        @DisplayName("Should update file owner successfully")
        void shouldUpdateFileOwnerSuccessfully() {
            // Arrange
            UUID fileId = UUID.randomUUID();
            UUID newOwnerId = UUID.randomUUID();

            FileMetaData existingFileMetaData = FileMetaData.builder().id(fileId)
                    .ownerId(testOwnerId).userId(testUserId).build();

            when(fileMetaDataRepository.findById(fileId))
                    .thenReturn(Optional.of(existingFileMetaData));

            // Act
            fileMetaDataService.updateFileOwner(fileId, newOwnerId);

            // Assert
            assertThat(existingFileMetaData.getOwnerId()).isEqualTo(newOwnerId);
            verify(fileMetaDataRepository).findById(fileId);
        }

        @Test
        @DisplayName("Should throw FileException when file not found")
        void shouldThrowFileExceptionWhenFileNotFound() {
            // Arrange
            UUID nonExistentFileId = UUID.randomUUID();
            UUID newOwnerId = UUID.randomUUID();

            when(fileMetaDataRepository.findById(nonExistentFileId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(
                    () -> fileMetaDataService.updateFileOwner(nonExistentFileId, newOwnerId))
                    .isInstanceOf(FileException.class).hasMessageContaining("File not found");

            verify(fileMetaDataRepository).findById(nonExistentFileId);
        }
    }
}
