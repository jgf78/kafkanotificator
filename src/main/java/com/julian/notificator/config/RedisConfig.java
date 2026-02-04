package com.julian.notificator.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.julian.notificator.model.tdt.TdtProgramme;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Tiempo de vida de la cache
                .disableCachingNullValues();    // No cachea valores null

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
    
    @Bean
    RedisTemplate<String, TdtProgramme> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, TdtProgramme> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializador para la clave
        template.setKeySerializer(new StringRedisSerializer());

        // Serializador JSON para TdtProgramme con soporte Java 8 date/time
        Jackson2JsonRedisSerializer<TdtProgramme> serializer =
                new Jackson2JsonRedisSerializer<>(TdtProgramme.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        serializer.setObjectMapper(objectMapper);

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

}

