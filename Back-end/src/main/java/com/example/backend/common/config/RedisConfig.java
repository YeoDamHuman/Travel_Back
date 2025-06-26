package com.example.backend.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);//Redis 서버 연결

        // 키와 값을 문자열로 직렬화하기 위한 설정
        template.setKeySerializer(new StringRedisSerializer()); //키를 문자열로 저장
        template.setValueSerializer(new StringRedisSerializer()); //값을 문자열로 저장

        return template;
    }
}
