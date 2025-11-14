package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.EmptyFileException;
import com.nexaworks.rafiq.service.ServiceImpl.GeminiService;
import com.nexaworks.rafiq.service.ServiceImpl.LabTestServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.PdfExtractorServiceImpl;
import com.nexaworks.rafiq.service.ServiceImpl.UserServiceImpl;
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

public class PdfExtractorServiceImplTest {
  @Mock LabTestServiceImpl labTestService;
  @Mock GeminiService geminiService;
  @Mock UserServiceImpl userService;
  @InjectMocks PdfExtractorServiceImpl pdfExtractorService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @DisplayName("Extract pdf should throw empty file exception when file is empty")
  @Test
  void extractPdf_ShouldThrowEmptyFileException_WhenFileIsEmpty() {
    MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
    assertThrows(EmptyFileException.class, () -> pdfExtractorService.extractPdf(emptyFile));
  }

  @DisplayName("Extract pdf should convert to pdf if the file is an image")
  @Test
  void extractPdf_ShouldConvertImageToPdf_WhenFileIsImage()
      throws IOException, DocumentException, ExecutionException, InterruptedException {
    MockMultipartFile imageFile =
        new MockMultipartFile("file", "test.png", "image/png", createMinimalPngImage());
    User user = new User();
    UUID id = UUID.randomUUID();

    when(labTestService.saveTestPdf(imageFile, user))
        .thenReturn(CompletableFuture.completedFuture(id));
    when(userService.getUser()).thenReturn(user);
    when(geminiService.extractLabResultsFromPdf(any())).thenReturn("{}");

    String result = pdfExtractorService.extractPdf(imageFile);
    String actualResult = "{\"testId\":\"" + id + "\"}";

    assertEquals(actualResult, result);
    verify(geminiService)
        .extractLabResultsFromPdf(
            argThat(
                bytes ->
                    bytes != null && !java.util.Arrays.equals(bytes, createMinimalPngImage())));
  }

  private byte[] createMinimalPngImage() {
    return new byte[] {
      (byte) 0x89,
      0x50,
      0x4E,
      0x47,
      0x0D,
      0x0A,
      0x1A,
      0x0A, // PNG signature
      0x00,
      0x00,
      0x00,
      0x0D,
      0x49,
      0x48,
      0x44,
      0x52, // IHDR chunk
      0x00,
      0x00,
      0x00,
      0x01,
      0x00,
      0x00,
      0x00,
      0x01, // 1x1 dimensions
      0x08,
      0x06,
      0x00,
      0x00,
      0x00,
      0x1F,
      0x15,
      (byte) 0xC4,
      (byte) 0x89,
      0x00,
      0x00,
      0x00,
      0x0A,
      0x49,
      0x44,
      0x41,
      0x54, // IDAT chunk
      0x78,
      (byte) 0x9C,
      0x63,
      0x00,
      0x01,
      0x00,
      0x00,
      0x05,
      0x00,
      0x01,
      0x0D,
      0x0A,
      0x2D,
      (byte) 0xB4,
      0x00,
      0x00,
      0x00,
      0x00,
      0x49,
      0x45,
      0x4E,
      0x44, // IEND chunk
      (byte) 0xAE,
      0x42,
      0x60,
      (byte) 0x82
    };
  }
}
