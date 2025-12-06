package com.nexaworks.rafiq.fileManagment.entity;

import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;

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
public class File extends BaseEntity {
    private String name;
    private String publicId;
    private String url;
    private String type;
    private UUID userId;

}
