package com.nexaworks.rafiq.dto.response;

public record AddResponse<T>(boolean success, String message, T data) {
}
