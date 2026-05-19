package com.nexaworks.rafiq.service.consultation;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.exception.custom.consultation.ConsultationSlotTakenException;
import com.nexaworks.rafiq.repository.ConsultationSlotRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultationSlotHoldingService implements IConsultationSlotHoldingService {

    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final AuthService authService;
    private final ConsultationSlotRepository slotRepository;

    private static final long LEASE_TIME = 90;
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    private String lockKey(UUID slotId) {
        return "slot:lock:" + slotId;
    }
    private String holderKey(UUID slotId) {
        return "slot:holder:" + slotId;
    }

    @Override
    public void hold(UUID slotId) {
        UUID userId = authService.getAuthenticateUserId();
        if (slotRepository.isBooked(slotId)) {
            throw new ConsultationSlotTakenException("Slot is booked");
        }

        log.info("User [{}] attempting to hold slot [{}]", userId, slotId);
        try {
            RLock lock = redissonClient.getLock(lockKey(slotId));
            boolean acquired = lock.tryLock(0, LEASE_TIME, TIME_UNIT);

            if (acquired) {
                redisTemplate.opsForValue().set(holderKey(slotId), userId.toString(), LEASE_TIME,
                        TIME_UNIT);
                log.info("Slot [{}] held by user [{}]", slotId, userId);
            } else {
                log.warn("Slot [{}] already held — user [{}] blocked", slotId, userId);
                throw new ConsultationSlotTakenException("Slot is already booked");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void release(UUID slotId) {
        UUID userId = authService.getAuthenticateUserId();
        log.info("User [{}] releasing slot [{}]", userId, slotId);

        String holder = redisTemplate.opsForValue().get(holderKey(slotId));

        if (holder != null && holder.equals(userId.toString())) {
            RLock lock = redissonClient.getLock(lockKey(slotId));
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            redisTemplate.delete(holderKey(slotId)); // clean up holder record
            log.info("Slot [{}] released by user [{}]", slotId, userId);
        } else {
            log.warn("User [{}] tried to release slot [{}] they don't own", userId, slotId);
        }
    }
}
