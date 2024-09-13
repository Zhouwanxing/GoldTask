package com.zhou.goldtask.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> objectObjectRedisTemplate = new RedisTemplate<>();
        objectObjectRedisTemplate.setConnectionFactory(factory);
        //序列化配置
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        //接受单个字符的值反序列化为数组
        //om.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //String序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //设置key的序列化器
        objectObjectRedisTemplate.setKeySerializer(stringRedisSerializer);
        //设置hash key的序列化
        objectObjectRedisTemplate.setHashKeySerializer(stringRedisSerializer);
        //设置value的序列化器(json字符串会解析为map)
        //objectObjectRedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        //json字符串解析为字符串
        objectObjectRedisTemplate.setValueSerializer(RedisSerializer.string());
        //设置hash value的序列化
        objectObjectRedisTemplate.setHashValueSerializer(stringRedisSerializer);
        objectObjectRedisTemplate.afterPropertiesSet();
        return objectObjectRedisTemplate;
    }
}