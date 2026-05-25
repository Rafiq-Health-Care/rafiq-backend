package com.nexaworks.rafiq.service.messagelog;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.MessageLog;
import com.nexaworks.rafiq.rabbit.enums.DLQAction;
import com.nexaworks.rafiq.repository.MessageLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageLogService implements IMessageLogService {
    private final MessageLogRepository messageLogRepository;
    @Override
    @Transactional
    public void persist(String failureReason, String payload, Map<String, Object> headers,
            DLQAction action, UUID messageId, String queueName) {
        log.info("Persisting message to message log");
        MessageLog messageLog = MessageLog.builder().id(messageId).payload(payload)
                .dlqAction(action).headers(headers.toString()).failureReason(failureReason)
                .sourceQueue(queueName).build();
        try {
            messageLogRepository.save(messageLog);
        } catch (Exception e) {
            log.error("Failed to persist message to message log");
        }
    }
}
