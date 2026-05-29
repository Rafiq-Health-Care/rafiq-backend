package com.nexaworks.rafiq.integration.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.nexaworks.rafiq.scheduler.ExpirationScheduler;
import com.nexaworks.rafiq.service.call.RtcProvider;

@TestConfiguration(proxyBeanMethods = false)
@Profile("test")
public class IntegrationTestExternalMocks {

    @Bean
    @Primary
    public Cloudinary cloudinaryClientMock() throws IOException {
        Cloudinary cloudinary = Mockito.mock(Cloudinary.class);
        Uploader uploader = Mockito.mock(Uploader.class);
        Mockito.when(cloudinary.uploader()).thenReturn(uploader);
        Mockito.when(uploader.upload(Mockito.any(byte[].class), Mockito.anyMap()))
                .thenReturn(Map.of("secure_url",
                        "https://res.cloudinary.com/integration-test/image/upload/v123/mock.png",
                        "public_id", "integration-test/mock-public-id"));
        Mockito.when(uploader.destroy(Mockito.anyString(), Mockito.anyMap()))
                .thenReturn(Collections.emptyMap());
        return cloudinary;
    }

    @Bean
    @Primary
    public RtcProvider rtcProviderMock() {
        RtcProvider mock = Mockito.mock(RtcProvider.class);
        Mockito.when(mock.generateToken(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("integration-test-mock-rtc-token");
        return mock;
    }

    @Bean
    @Primary
    public ExpirationScheduler expirationSchedulerMock() {
        return Mockito.mock(ExpirationScheduler.class);
    }

    @Bean
    @Primary
    public RedissonClient redissonClient() {
        return Mockito.mock(RedissonClient.class);
    }
}
