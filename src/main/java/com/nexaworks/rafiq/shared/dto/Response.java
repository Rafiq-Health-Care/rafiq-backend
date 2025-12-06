package com.nexaworks.rafiq.shared.dto;

public record Response<T>(Boolean success, T data) {
}
