package com.nexaworks.rafiq.idempotency.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nexaworks.rafiq.idempotency.filter.IdempotencyFilter;
import com.nexaworks.rafiq.idempotency.filter.WrapperFilter;
import com.nexaworks.rafiq.idempotency.storage.IdempotencyStore;
import com.nexaworks.rafiq.idempotency.storage.InMemoryIdempotencyStore;

@Configuration
@Import(RedisIdempotencyConfiguration.class)
public class IdempotencyConfiguration {
    @Bean
    @ConditionalOnMissingBean({IdempotencyStore.class, RedisConnectionFactory.class})
    public IdempotencyStore inMemoryIdempotencyStore(IdempotencyProperties properties) {
        return new InMemoryIdempotencyStore(properties.getTtl().getSeconds());
    }

    @Bean
    public IdempotencyFilter idempotencyFilter(IdempotencyStore idempotencyStore) {
        return new IdempotencyFilter(idempotencyStore);
    }
    @Bean
    public WrapperFilter wrapperFilter(IdempotencyStore idempotencyStore) {
        return new WrapperFilter(idempotencyStore);
    }
    @Bean
    public WebMvcConfigurer idempotencyWebMvcConfigurer(IdempotencyFilter idempotencyFilter) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(idempotencyFilter);
            }
        };
    }

}
