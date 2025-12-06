package com.nexaworks.rafiq.notification.service.implementation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("mobile")
@Slf4j
@RequiredArgsConstructor
public class MobileNotificationService implements NotificationService {
}
