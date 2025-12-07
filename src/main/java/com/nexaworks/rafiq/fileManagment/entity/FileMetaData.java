package com.nexaworks.rafiq.fileManagment.entity;

import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;

import com.nexaworks.rafiq.shared.entity.FileCategory;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class FileMetaData extends BaseEntity {
    private String originalFileName;
    private String cloudinaryPublicId;
    private String cloudinarySecureUrl;
    private Long fileSize;
    private String mimeType;
    private FileCategory category;
    private UUID ownerId;
    private UUID userId;
}
