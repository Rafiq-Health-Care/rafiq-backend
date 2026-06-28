package com.nexaworks.rafiq.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nexaworks.rafiq.constant.CacheNames;

@Configuration
public class RedisConfig {

    private static final ObjectMapper REDIS_MAPPER;
    static {
        REDIS_MAPPER = new ObjectMapper();
        REDIS_MAPPER.findAndRegisterModules();
        REDIS_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        REDIS_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(REDIS_MAPPER)))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CacheNames.CONSULTATION, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put(CacheNames.DOCTOR_PROFILE, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put(CacheNames.DOCTOR_SEARCH, defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put(CacheNames.DOCTOR_AVAILABLE_SLOTS,
                defaultConfig.entryTtl(Duration.ofSeconds(30)));
        cacheConfigs.put(CacheNames.DRUG_SEARCH, defaultConfig.entryTtl(Duration.ofHours(6)));
        cacheConfigs.put(CacheNames.SPECIALIZATIONS, defaultConfig.entryTtl(Duration.ofDays(1)));

        return RedisCacheManager.builder(cf).cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs).build();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(
                        "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setDatabase(redisProperties.getDatabase()).setConnectionPoolSize(8)
                .setConnectionMinimumIdleSize(2).setConnectTimeout(3000).setTimeout(3000);

        if (StringUtils.hasText(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }

        return Redisson.create(config);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
