package com.linck.discovery;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.dynamic.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(RedisDiscoveryProperties.class)
@ConditionalOnClass({ StringRedisTemplate.class, Caffeine.class })
@AutoConfigureAfter(DataRedisAutoConfiguration.class)
public class RedisDiscoveryAutoConfiguration {

    @Bean
    public RedisServiceRegistry redisServiceRegistry(StringRedisTemplate redisTemplate, RedisDiscoveryProperties properties) {
        return new RedisServiceRegistry(redisTemplate, properties);
    }

    @Bean
    public RedisDiscoveryClient redisDiscoveryClient(StringRedisTemplate redisTemplate, RedisDiscoveryProperties properties) {
        return new RedisDiscoveryClient(redisTemplate, properties);
    }

    // --- 必须要有的：触发自动注册的 Bean ---

    @Bean
    public RedisAutoServiceRegistration redisAutoServiceRegistration(
            ApplicationContext context,
            RedisServiceRegistry registry,
            RedisDiscoveryProperties properties,
            RedisRegistration registration) {
        return new RedisAutoServiceRegistration(context, registry, properties, registration);
    }

    @Bean
    public RedisRegistration redisRegistration(
            @Value("${spring.application.name}") String serviceId,
            @Value("${server.port:8080}") int port,
            InetUtils inetUtils) {
        // 使用 Spring Cloud 自带的 InetUtils 获取本机 IP
        String host = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        String instanceId = UUID.randomUUID().toString();
        return new RedisRegistration(serviceId, instanceId, host, port, new HashMap<>());
    }
}