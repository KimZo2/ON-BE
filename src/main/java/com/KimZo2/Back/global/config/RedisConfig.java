package com.KimZo2.Back.global.config;

import com.KimZo2.Back.global.listener.RedisKeyExpirationListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean(name = "redisConnectionFactoryDb0")
    @Primary
    public RedisConnectionFactory redisConnectionFactoryDb0() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(host, port);
        lettuceConnectionFactory.setDatabase(0);
        return lettuceConnectionFactory;
    }

    @Bean(name = "redisConnectionFactoryDb1")
    public RedisConnectionFactory redisConnectionFactoryDb1() {
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(host, port);
        lettuceConnectionFactory.setDatabase(1);
        return lettuceConnectionFactory;
    }

    @Bean(name = "redisTemplate")
    @Primary
    public RedisTemplate<String, Object> redisTemplate(@Qualifier("redisConnectionFactoryDb0") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean(name = "redisTemplateForDb1")
    public RedisTemplate<String, Object> redisTemplateForDb1(@Qualifier("redisConnectionFactoryDb1") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("redisConnectionFactoryDb0") RedisConnectionFactory connectionFactory,
            RedisKeyExpirationListener expirationListener) { // 작성하신 리스너 클래스 주입

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // __keyevent@0__:expired -> 0번 DB 만료만 감시
        // __keyevent@*__:expired -> 모든 DB 만료 감시
        container.addMessageListener(expirationListener, new PatternTopic("__keyevent@*__:expired"));

        return container;
    }
}
