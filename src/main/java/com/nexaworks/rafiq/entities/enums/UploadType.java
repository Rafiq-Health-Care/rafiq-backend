package com.nexaworks.rafiq.entities.enums;

import lombok.Getter;

@Getter
public enum UploadType {
    IMAGE("image"), VIDEO("video"), DOCUMENT("raw"), PDF("raw"), VOICE("video");

    private final String cloudinaryType;

    UploadType(String cloudinaryType) {
        this.cloudinaryType = cloudinaryType;
    }
}
