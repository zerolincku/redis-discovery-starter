package com.linck.discovery;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisDiscoveryClient implements DiscoveryClient {

    private final StringRedisTemplate redisTemplate;
    private final RedisDiscoveryProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Caffeine 缓存：Key是服务名，Value是服务实例列表
    private final Cache<String, List<ServiceInstance>> localCache;

    public RedisDiscoveryClient(StringRedisTemplate redisTemplate, RedisDiscoveryProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;

        // 初始化 Caffeine
        this.localCache = Caffeine.newBuilder()
                .expireAfterWrite(properties.getCacheExpire(), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String description() {
        return "Redis Discovery Client";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        // 1. 优先查 Caffeine 本地缓存
        return localCache.get(serviceId, this::loadFromRedis);
    }

    @Override
    public List<String> getServices() {
        // 简单实现：扫描 Key。注意：KEYS 命令在生产大数量级时慎用，建议用 SCAN
        String pattern = properties.getPrefix() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        // 解析 Key 获取 ServiceId 部分 (discovery:serviceId:instanceId)
        return keys.stream()
                .map(k -> k.split(":")[1])
                .distinct()
                .collect(Collectors.toList());
    }

    // --- 私有方法：从 Redis 加载数据 ---

    private List<ServiceInstance> loadFromRedis(String serviceId) {
        // 1. 扫描该服务的所有实例 Key
        String pattern = properties.getPrefix() + ":" + serviceId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }

        List<ServiceInstance> instances = new ArrayList<>();
        // 2. 批量获取 Values (MGET 性能更好)
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        if (values != null) {
            for (String val : values) {
                if (val != null) {
                    try {
                        RedisRegistration ins = objectMapper.readValue(val, RedisRegistration.class);
                        instances.add(ins);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instances;
    }
}