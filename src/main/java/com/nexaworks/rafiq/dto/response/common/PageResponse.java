package com.nexaworks.rafiq.dto.response.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic paginated response wrapper")
public record PageResponse<T>(

        @Schema(description = "List of items in the current page") List<T> content,

        @Schema(description = "Number of elements in the current page") int numberOfElements,

        @Schema(description = "Page size") int size,

        @Schema(description = "Total number of pages") int totalPages,

        @Schema(description = "Whether this is the last page") boolean lastPage,

        @Schema(description = "Whether this is the first page") boolean firstPage) {

    public static <T> PageResponse<T> of(Page<?> page, List<T> content) {
        return new PageResponse<>(content, page.getNumberOfElements(), page.getSize(),
                page.getTotalPages(), page.isLast(), page.isFirst());
    }

    public static <S, T> PageResponse<T> of(Page<S> page, Function<? super S, T> mapper) {
        return of(page, page.getContent().stream().map(mapper).toList());
    }
}