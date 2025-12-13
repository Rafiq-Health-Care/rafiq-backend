package com.nexaworks.rafiq.fileManagment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.fileManagment.api.dto.FileResponse;
import com.nexaworks.rafiq.fileManagment.entity.FileMetaData;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "fileType", source = "mimeType")
    @Mapping(target = "size", source = "fileSize")
    @Mapping(target = "fileUrl", source = "cloudinarySecureUrl")
    @Mapping(target = "fileName", source = "originalFileName")
    FileResponse toDto(FileMetaData fileMetaData);
}
