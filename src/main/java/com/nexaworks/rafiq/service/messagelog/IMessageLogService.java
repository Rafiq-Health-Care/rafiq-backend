package com.nexaworks.rafiq.service.messagelog;

import java.util.Map;
import java.util.UUID;

import com.nexaworks.rafiq.rabbit.enums.DLQAction;

public interface IMessageLogService {
    void persist(String failureReason, String payload, Map<String, Object> headers,
            DLQAction action, UUID messageId, String queueName);
}
