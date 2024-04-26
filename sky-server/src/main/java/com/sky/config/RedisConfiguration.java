package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redisTemplate对象");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置redis连接对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置key序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置value序列化方式
        // redisTemplate.setValueSerializer(new StringRedisSerializer());
        // 配置生效
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
