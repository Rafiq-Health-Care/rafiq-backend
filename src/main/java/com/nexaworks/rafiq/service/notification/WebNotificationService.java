package com.nexaworks.rafiq.service.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("web")
@Slf4j
@RequiredArgsConstructor
public class WebNotificationService implements NotificationService {
}
