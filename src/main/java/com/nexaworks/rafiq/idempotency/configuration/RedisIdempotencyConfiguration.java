package com.nexaworks.rafiq.idempotency.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.idempotency.storage.IdempotencyStore;
import com.nexaworks.rafiq.idempotency.storage.RedisIdempotencyStore;

@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisIdempotencyConfiguration {

    @Bean("idempotencyRedisTemplate")
    @ConditionalOnProperty(prefix = "idempotency", name = "store-type", havingValue = "redis")
    public RedisTemplate<String, String> idempotencyRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    public IdempotencyStore redisIdempotencyStore(
            @Qualifier("idempotencyRedisTemplate") RedisTemplate<String, String> template,
            ObjectMapper objectMapper, IdempotencyProperties properties) {
        return new RedisIdempotencyStore(template, objectMapper, properties.getTtl().toSeconds());
    }
}
