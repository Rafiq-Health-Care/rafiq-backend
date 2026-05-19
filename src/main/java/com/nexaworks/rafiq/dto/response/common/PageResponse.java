package com.nexaworks.rafiq.dto.response.common;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic paginated response wrapper")
public record PageResponse<T>(

        @Schema(description = "List of items in the current page") List<T> content,

        @Schema(description = "Number of elements in the current page") int numberOfElements,

        @Schema(description = "Page size") int size,

        @Schema(description = "Total number of pages") int totalPages,

        @Schema(description = "Whether this is the last page") boolean lastPage,

        @Schema(description = "Whether this is the first page") boolean firstPage) {
}