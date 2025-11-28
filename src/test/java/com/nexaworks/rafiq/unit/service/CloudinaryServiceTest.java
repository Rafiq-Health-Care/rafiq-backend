package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.enums.UploadType;
import com.nexaworks.rafiq.exception.custom.EmptyFileException;
import com.nexaworks.rafiq.exception.custom.FileException;
import com.nexaworks.rafiq.exception.custom.FileUploadException;
import com.nexaworks.rafiq.service.ServiceImpl.CloudinaryService;

@DisplayName("CloudinaryService Test Cases")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Should upload resource successfully")
    @Test
    void shouldUploadResourceSuccessfully() throws IOException {

        when(file.isEmpty()).thenReturn(false);

        byte[] mockBytes = new byte[10];
        when(file.getBytes()).thenReturn(mockBytes);

        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "secure_url", "https://example.com/image.jpg",
                        "public_id", "abc123"));

        UploadResults result = cloudinaryService.uploadResource(file, UploadType.IMAGE);

        assertEquals("https://example.com/image.jpg", result.url());
        assertEquals("abc123", result.publicId());
    }

    @DisplayName("Should throw exception when file is empty")
    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        when(file.isEmpty()).thenReturn(true);
        assertThrows(EmptyFileException.class, () -> cloudinaryService.uploadResource(file, UploadType.IMAGE));
    }

    @DisplayName("Should throw exception when upload fails")
    @Test
    void shouldThrowExceptionWhenUploadFails() throws IOException {
        when(file.isEmpty()).thenReturn(false);

        byte[] mockBytes = new byte[10];
        when(file.getBytes()).thenReturn(mockBytes);

        when(cloudinary.uploader()).thenReturn(uploader);

        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        assertThrows(FileUploadException.class, () -> cloudinaryService.uploadResource(file, UploadType.IMAGE));
    }

    @DisplayName("Should delete resource without exception")
    @Test
    void shouldDeleteResourceWithoutException() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap())).thenReturn(Map.of("result", "ok"));

        assertDoesNotThrow(() -> cloudinaryService.delete("abc123"));
    }

    @DisplayName("Should throw exception when delete fails")
    @Test
    void shouldThrowExceptionWhenDeleteFails() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete failed"));
        assertThrows(FileException.class, () -> cloudinaryService.delete("abc123"));
    }
}
