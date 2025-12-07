package com.nexaworks.rafiq.labTest.api.dto;

import java.util.UUID;

public record TestResponse(String name, UUID testId, String fileId) {
}
