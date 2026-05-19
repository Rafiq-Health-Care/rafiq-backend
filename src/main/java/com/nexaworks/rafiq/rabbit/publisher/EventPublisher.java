package com.nexaworks.rafiq.rabbit.publisher;

public interface EventPublisher<T> {
    void publish(T event);

}
