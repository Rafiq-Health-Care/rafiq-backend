package com.nexaworks.rafiq.dto.response.common;

public record Response<T>(Boolean success, T data) {
}
