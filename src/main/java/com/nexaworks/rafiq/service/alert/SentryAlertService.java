package com.nexaworks.rafiq.service.alert;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.enums.Level;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
@Component
public class SentryAlertService implements AlertService {
    @Override
    public void sendAlert(String title, String message, Level level) {
        setupAlert(title, message, level);

    }

    @Override
    public void sendAlert(String title, String message, Map<String, Object> alertData,
            Level level) {
        Sentry.withScope(scope -> {
            alertData.forEach((key, value) -> scope.setTag(key, value.toString()));
            setupAlert(title, message, level);

        });

    }

    private void setupAlert(String title, String message, Level level) {
        SentryEvent alert = new SentryEvent();
        Message msg = new Message();
        msg.setMessage(title + ": " + message);
        alert.setMessage(msg);
        SentryLevel level1 = SentryLevel.valueOf(level.name());
        alert.setLevel(level1);
        Sentry.captureEvent(alert);
    }
}
