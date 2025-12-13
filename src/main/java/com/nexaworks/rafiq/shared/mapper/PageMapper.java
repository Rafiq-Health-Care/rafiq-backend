package com.nexaworks.rafiq.shared.mapper;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.shared.dto.PageResponse;

/**
 * Generic page mapper that converts Spring Data Page objects to custom
 * PageResponse DTOs. This mapper is fully generic and does not depend on any
 * domain-specific entities, maintaining proper module boundaries in the modular
 * monolith architecture.
 */
@Component
public class PageMapper {

    /**
     * Maps a Spring Data Page to a PageResponse DTO using the provided mapper
     * function.
     *
     * @param page
     *            the Spring Data Page object
     * @param mapper
     *            the function to convert entity to DTO
     * @param <T>
     *            the entity type
     * @param <R>
     *            the DTO type
     * @return PageResponse containing the mapped content and pagination metadata
     */
    public <T, R> PageResponse<R> map(Page<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                (int) page.getTotalElements(), page.getSize(), page.getTotalPages(), page.isLast(),
                page.isFirst());
    }
}
