package com.nexaworks.rafiq.shared.dto;

import java.util.List;

public record PageResponse<T>(List<T> content, int numberOfElements, int size, int totalPages,
        boolean lastPage, boolean firstPage) {
}
