package com.nexaworks.rafiq.test.fileManagment.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.ai.service.AiService;
import com.nexaworks.rafiq.fileManagment.exception.EmptyFileException;
import com.nexaworks.rafiq.fileManagment.service.FileService;
import com.nexaworks.rafiq.fileManagment.service.implementation.PdfExtractorServiceImpl;

@DisplayName("PdfExtractorService Test Cases")
class PdfExtractorServiceImplTest {
    @Mock
    FileService fileService;

    @Mock
    AiService aiService;

    @InjectMocks
    PdfExtractorServiceImpl pdfExtractorService;

    private UUID patientId;
    private UUID fileId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
        fileId = UUID.randomUUID();
    }

    @DisplayName("Extract pdf should throw empty file exception when file is empty")
    @Test
    void extractPdf_ShouldThrowEmptyFileException_WhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        assertThrows(EmptyFileException.class,
                () -> pdfExtractorService.extractPdf(emptyFile, patientId));
    }

    @DisplayName("Extract pdf should convert to pdf if the file is an image")
    @Test
    void extractPdf_ShouldConvertImageToPdf_WhenFileIsImage()
            throws IOException, DocumentException, ExecutionException, InterruptedException {
        MockMultipartFile imageFile = new MockMultipartFile("file", "test.png", "image/png",
                createMinimalPngImage());

        when(fileService.saveFileAsync(imageFile, patientId))
                .thenReturn(CompletableFuture.completedFuture(fileId));
        when(aiService.extractLabResultsFromPdf(any())).thenReturn("{\"test\":\"result\"}");

        String result = pdfExtractorService.extractPdf(imageFile, patientId);

        ObjectMapper mapper = new ObjectMapper();
        String expectedResult = mapper
                .readTree("{\"test\":\"result\",\"fileId\":\"" + fileId + "\"}").toString();

        assertEquals(expectedResult, result);
        verify(aiService).extractLabResultsFromPdf(argThat(bytes -> bytes != null
                && !java.util.Arrays.equals(bytes, createMinimalPngImage())));
    }

    @DisplayName("Extract pdf should process PDF file directly without conversion")
    @Test
    void extractPdf_ShouldProcessPdfDirectly_WhenFileIsPdf()
            throws IOException, DocumentException, ExecutionException, InterruptedException {
        byte[] pdfBytes = createMinimalPdfBytes();
        MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf",
                pdfBytes);

        when(fileService.saveFileAsync(pdfFile, patientId))
                .thenReturn(CompletableFuture.completedFuture(fileId));
        when(aiService.extractLabResultsFromPdf(any())).thenReturn("{\"test\":\"result\"}");

        String result = pdfExtractorService.extractPdf(pdfFile, patientId);

        ObjectMapper mapper = new ObjectMapper();
        String expectedResult = mapper
                .readTree("{\"test\":\"result\",\"fileId\":\"" + fileId + "\"}").toString();

        assertEquals(expectedResult, result);
        verify(aiService).extractLabResultsFromPdf(pdfBytes);
    }

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
