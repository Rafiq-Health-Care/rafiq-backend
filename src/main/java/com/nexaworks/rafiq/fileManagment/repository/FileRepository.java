package com.nexaworks.rafiq.fileManagment.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.fileManagment.entity.File;

public interface FileRepository extends JpaRepository<File, UUID> {
}
