package com.nexaworks.rafiq.dto.response;

import java.util.UUID;

public record TestResponse(String name, UUID testId, String fileUrl, String fileType) {
}
