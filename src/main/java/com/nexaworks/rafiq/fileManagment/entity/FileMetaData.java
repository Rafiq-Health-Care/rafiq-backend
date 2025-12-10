package com.nexaworks.rafiq.fileManagment.entity;

import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import com.nexaworks.rafiq.shared.entity.FileCategory;

import jakarta.persistence.*;
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
@Table(name = "file_metadata", schema = "file_managment_schema")
public class FileMetaData extends BaseEntity {
    @Column(name = "file_name")
    private String originalFileName;
    @Column(name = "public_id")
    private String cloudinaryPublicId;
    @Column(name = "file_url")
    private String cloudinarySecureUrl;
    private Long fileSize;
    private String mimeType;
    @Enumerated(EnumType.STRING)
    private FileCategory category;
    private UUID ownerId;
    private UUID userId;
}
