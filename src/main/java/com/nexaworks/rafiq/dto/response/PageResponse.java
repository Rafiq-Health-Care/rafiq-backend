package com.nexaworks.rafiq.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content, int numberOfElements, int size, int totalPages, boolean lastPage, boolean firstPage) {}
