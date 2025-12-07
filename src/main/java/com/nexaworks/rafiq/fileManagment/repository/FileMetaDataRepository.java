package com.nexaworks.rafiq.fileManagment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.fileManagment.entity.FileMetaData;

public interface FileMetaDataRepository extends JpaRepository<FileMetaData, UUID> {
}
