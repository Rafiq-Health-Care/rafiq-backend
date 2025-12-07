package com.nexaworks.rafiq.test.fileManagment.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.ai.service.AiFacade;
import com.nexaworks.rafiq.fileManagment.exception.EmptyFileException;
import com.nexaworks.rafiq.fileManagment.service.FileMetaDataService;
import com.nexaworks.rafiq.fileManagment.service.implementation.PdfExtractorServiceImpl;
import com.nexaworks.rafiq.shared.entity.FileCategory;

@ExtendWith(MockitoExtension.class)
@DisplayName("PdfExtractorService Unit Tests")
class PdfExtractorServiceImplTest {

    @Mock
    private FileMetaDataService fileMetaDataService;

    @Mock
    private AiFacade aiFacade;

    @InjectMocks
    private PdfExtractorServiceImpl pdfExtractorService;

    private UUID patientId;
    private UUID fileId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        fileId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Extract PDF Tests")
    class ExtractPdfTests {

        @Test
        @DisplayName("Should throw EmptyFileException when file is empty")
        void shouldThrowEmptyFileExceptionWhenFileIsEmpty() {
            // Arrange
            MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

            // Act & Assert
            assertThatThrownBy(() -> pdfExtractorService.extractPdf(emptyFile, patientId))
                    .isInstanceOf(EmptyFileException.class)
                    .hasMessageContaining("The provided PDF file is empty");
        }

        @Test
        @DisplayName("Should process PDF file directly without conversion")
        void shouldProcessPdfDirectlyWhenFileIsPdf()
                throws IOException, DocumentException, ExecutionException, InterruptedException {
            // Arrange
            byte[] pdfBytes = createMinimalPdfBytes();
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    pdfBytes);

            String aiResponse = "{\"name\":\"Blood Test\",\"date\":\"2024-01-15\",\"tests\":[]}";

            when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(fileId);
            when(aiFacade.extractLabResultsFromPdf(pdfBytes)).thenReturn(aiResponse);

            // Act
            String result = pdfExtractorService.extractPdf(pdfFile, patientId);

            // Assert
            ObjectMapper mapper = new ObjectMapper();
            String expectedResult = mapper.readTree(
                    "{\"name\":\"Blood Test\",\"date\":\"2024-01-15\",\"tests\":[],\"fileId\":\""
                            + fileId + "\"}")
                    .toString();

            assertThat(result).isEqualTo(expectedResult);

            // Verify interactions
            verify(fileMetaDataService).saveFile(pdfFile, null, FileCategory.LAB_TEST, patientId);
            verify(aiFacade).extractLabResultsFromPdf(pdfBytes);
        }

        @Test
        @DisplayName("Should convert image to PDF before processing")
        void shouldConvertImageToPdfWhenFileIsImage()
                throws IOException, DocumentException, ExecutionException, InterruptedException {
            // Arrange
            byte[] imageBytes = createMinimalPngImage();
            MockMultipartFile imageFile = new MockMultipartFile("file", "test.png", "image/png",
                    imageBytes);

            String aiResponse = "{\"name\":\"Lab Result\",\"tests\":[]}";

            when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(fileId);
            when(aiFacade.extractLabResultsFromPdf(any(byte[].class))).thenReturn(aiResponse);

            // Act
            String result = pdfExtractorService.extractPdf(imageFile, patientId);

            // Assert
            assertThat(result).contains("\"fileId\":\"" + fileId + "\"");
            assertThat(result).contains("\"name\":\"Lab Result\"");
            assertThat(result).contains("\"tests\":[]");

            // Verify file was saved
            verify(fileMetaDataService).saveFile(imageFile, null, FileCategory.LAB_TEST, patientId);

            // Verify AI was called with converted PDF bytes (not original image bytes)
            verify(aiFacade).extractLabResultsFromPdf(
                    argThat(bytes -> bytes != null && !java.util.Arrays.equals(bytes, imageBytes)
                            && bytes.length > imageBytes.length)); // PDF will be larger
        }

        // @Test
        // @DisplayName("Should convert JPEG image to PDF before processing")
        // void shouldConvertJpegImageToPdfWhenFileIsJpeg()
        // throws IOException, DocumentException, ExecutionException,
        // InterruptedException {
        // // Arrange
        // byte[] jpegBytes = "fake-jpeg-content".getBytes();
        // MockMultipartFile jpegFile = new MockMultipartFile("file", "test.jpg",
        // "image/jpeg",
        // jpegBytes);
        //
        // String aiResponse = "{\"name\":\"X-Ray Result\",\"tests\":[]}";
        //
        // when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
        // eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(fileId);
        // when(aiFacade.extractLabResultsFromPdf(any(byte[].class))).thenReturn(aiResponse);
        //
        // // Act
        // String result = pdfExtractorService.extractPdf(jpegFile, patientId);
        //
        // // Assert
        // assertThat(result).contains("\"fileId\":\"" + fileId + "\"");
        // assertThat(result).contains("\"name\":\"X-Ray Result\"");
        //
        // verify(fileMetaDataService).saveFile(jpegFile, null, FileCategory.LAB_TEST,
        // patientId);
        // verify(aiFacade).extractLabResultsFromPdf(any(byte[].class));
        // }

        @Test
        @DisplayName("Should save file asynchronously while processing")
        void shouldSaveFileAsynchronouslyWhileProcessing()
                throws IOException, DocumentException, ExecutionException, InterruptedException {
            // Arrange
            byte[] pdfBytes = createMinimalPdfBytes();
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    pdfBytes);

            String aiResponse = "{\"test\":\"result\"}";

            when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(fileId);
            when(aiFacade.extractLabResultsFromPdf(pdfBytes)).thenReturn(aiResponse);

            // Act
            String result = pdfExtractorService.extractPdf(pdfFile, patientId);

            // Assert - fileId should be added to the result
            assertThat(result).contains("\"fileId\":\"" + fileId + "\"");

            // Verify both operations completed
            verify(fileMetaDataService).saveFile(pdfFile, null, FileCategory.LAB_TEST, patientId);
            verify(aiFacade).extractLabResultsFromPdf(pdfBytes);
        }

        @Test
        @DisplayName("Should add fileId to AI extraction result JSON")
        void shouldAddFileIdToAiExtractionResultJson()
                throws IOException, DocumentException, ExecutionException, InterruptedException {
            // Arrange
            byte[] pdfBytes = createMinimalPdfBytes();
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    pdfBytes);

            String aiResponse = "{\"name\":\"Complete Blood Count\",\"date\":\"2024-01-15\",\"tests\":[{\"testName\":\"Hemoglobin\",\"result\":14.5}]}";
            UUID expectedFileId = UUID.randomUUID();

            when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(expectedFileId);
            when(aiFacade.extractLabResultsFromPdf(pdfBytes)).thenReturn(aiResponse);

            // Act
            String result = pdfExtractorService.extractPdf(pdfFile, patientId);

            // Assert - Parse result and verify structure
            ObjectMapper mapper = new ObjectMapper();
            var jsonNode = mapper.readTree(result);

            assertThat(jsonNode.has("name")).isTrue();
            assertThat(jsonNode.has("date")).isTrue();
            assertThat(jsonNode.has("tests")).isTrue();
            assertThat(jsonNode.has("fileId")).isTrue();
            assertThat(jsonNode.get("fileId").asText()).isEqualTo(expectedFileId.toString());
            assertThat(jsonNode.get("name").asText()).isEqualTo("Complete Blood Count");
        }

        @Test
        @DisplayName("Should pass null as ownerId when saving file")
        void shouldPassNullAsOwnerIdWhenSavingFile()
                throws IOException, DocumentException, ExecutionException, InterruptedException {
            // Arrange
            byte[] pdfBytes = createMinimalPdfBytes();
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    pdfBytes);

            when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(fileId);
            when(aiFacade.extractLabResultsFromPdf(any(byte[].class)))
                    .thenReturn("{\"test\":\"result\"}");

            // Act
            pdfExtractorService.extractPdf(pdfFile, patientId);

            // Assert - Verify ownerId is null (second parameter)
            verify(fileMetaDataService).saveFile(eq(pdfFile), isNull(), eq(FileCategory.LAB_TEST),
                    eq(patientId));
        }

        @Test
        @DisplayName("Should use LAB_TEST category when saving file")
        void shouldUseLabTestCategoryWhenSavingFile()
                throws IOException, DocumentException, ExecutionException, InterruptedException {
            // Arrange
            byte[] pdfBytes = createMinimalPdfBytes();
            MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                    pdfBytes);

            when(fileMetaDataService.saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId))).thenReturn(fileId);
            when(aiFacade.extractLabResultsFromPdf(any(byte[].class)))
                    .thenReturn("{\"test\":\"result\"}");

            // Act
            pdfExtractorService.extractPdf(pdfFile, patientId);

            // Assert - Verify LAB_TEST category is used
            verify(fileMetaDataService).saveFile(any(MultipartFile.class), isNull(),
                    eq(FileCategory.LAB_TEST), eq(patientId));
        }
    }

    // Helper methods
    private byte[] createMinimalPngImage() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, // PNG signature
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, // IHDR chunk
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, // 1x1 dimensions
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00,
                0x00, 0x0A, 0x49, 0x44, 0x41, 0x54, // IDAT chunk
                0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D,
                (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, // IEND chunk
                (byte) 0xAE, 0x42, 0x60, (byte) 0x82};
    }

    private byte[] createMinimalPdfBytes() {
        // Minimal PDF structure
        return "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\nxref\n0 4\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \ntrailer\n<< /Size 4 /Root 1 0 R >>\nstartxref\n179\n%%EOF"
                .getBytes();
    }
}
