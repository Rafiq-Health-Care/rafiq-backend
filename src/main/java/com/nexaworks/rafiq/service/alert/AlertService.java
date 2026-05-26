package com.nexaworks.rafiq.service.alert;

import java.util.Map;

import com.nexaworks.rafiq.entities.enums.Level;

public interface AlertService {
    void sendAlert(String title, String message, Level level);
    void sendAlert(String title, String message, Map<String, Object> alertData, Level level);
}
