package com.nexaworks.rafiq.medication.api.dto.response;

import java.util.UUID;

public record DrugSearchResponse(String name, UUID drugId) {
}
