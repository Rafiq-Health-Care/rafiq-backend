package com.nexaworks.rafiq.fileManagment.service.implementation;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.fileManagment.api.dto.UploadResults;
import com.nexaworks.rafiq.fileManagment.entity.FileMetaData;
import com.nexaworks.rafiq.fileManagment.entity.UploadType;
import com.nexaworks.rafiq.fileManagment.exception.FileException;
import com.nexaworks.rafiq.fileManagment.repository.FileMetaDataRepository;
import com.nexaworks.rafiq.fileManagment.service.FileMetaDataService;
import com.nexaworks.rafiq.shared.entity.FileCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileMetaDataServiceImpl implements FileMetaDataService {
    private final CloudinaryService cloudinaryService;
    private final FileMetaDataRepository fileMetaDataRepository;

    @Override
    public UUID saveFile(MultipartFile pdfFile, UUID ownerId, FileCategory fileCategory,
            UUID userId) {
        String fileType = pdfFile.getContentType();
        UploadType uploadType;
        if (fileType != null && fileType.startsWith("image")) {
            uploadType = UploadType.IMAGE;
        } else if (fileType != null && fileType.startsWith("application/pdf")) {
            uploadType = UploadType.PDF;
        } else {
            throw new FileException(
                    "Unsupported fileMetaData type, please upload a PDF or image fileMetaData.");
        }
        UploadResults uploadResults = cloudinaryService.uploadResource(pdfFile, uploadType);
        FileMetaData fileMetaData = FileMetaData.builder().fileSize(pdfFile.getSize())
                .originalFileName(pdfFile.getOriginalFilename()).category(fileCategory)
                .cloudinaryPublicId(uploadResults.publicId())
                .cloudinarySecureUrl(uploadResults.url()).ownerId(ownerId).userId(userId)
                .mimeType(fileType).build();
        return fileMetaDataRepository.save(fileMetaData).getId();
    }

    @Override
    @Transactional
    public void updateFileOwner(UUID fileId, UUID newOwnerId) {
        FileMetaData fileMetaData = fileMetaDataRepository.findById(fileId)
                .orElseThrow(() -> new FileException("File not found"));
        fileMetaData.setOwnerId(newOwnerId);
    }
}
