package com.linck.discovery;

import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedisServiceRegistry implements ServiceRegistry<RedisRegistration> {

    private final StringRedisTemplate redisTemplate;
    private final RedisDiscoveryProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 心跳定时器
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public RedisServiceRegistry(StringRedisTemplate redisTemplate, RedisDiscoveryProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public void register(RedisRegistration registration) {
        // 启动时立即注册一次
        sendHeartbeat(registration);

        // 开启定时任务，每隔 heartbeatInterval 秒执行一次
        heartbeatScheduler.scheduleAtFixedRate(
                () -> sendHeartbeat(registration),
                properties.getHeartbeatInterval(),
                properties.getHeartbeatInterval(),
                TimeUnit.SECONDS
        );
    }

    @Override
    public void deregister(RedisRegistration registration) {
        // 1. 停止心跳
        // 注意：生产环境需要更优雅地关闭线程池，这里简化处理
        // heartbeatScheduler.shutdown();

        // 2. 主动删除 Redis Key
        String key = buildKey(registration);
        redisTemplate.delete(key);
    }

    @Override
    public void close() {
        heartbeatScheduler.shutdownNow();
    }

    @Override
    public void setStatus(RedisRegistration registration, String status) {
        // 用于设置服务状态（UP/DOWN），暂略
    }

    @Override
    public <T> T getStatus(RedisRegistration registration) {
        return (T) "UP";
    }

    // --- 私有方法 ---

    private void sendHeartbeat(RedisRegistration registration) {
        try {
            String key = buildKey(registration);
            String value = objectMapper.writeValueAsString(registration);
            // SET key value EX 30
            // 无论 key 是否存在，直接覆盖，实现“无脑续约”和“自动注册”
            redisTemplate.opsForValue().set(key, value, properties.getServerExpire(), TimeUnit.SECONDS);
        } catch (Exception e) {
            // 这里的异常要吞掉或者打印日志，不能抛出导致应用崩溃
            // 只要下一次心跳成功，服务就能恢复
            System.err.println("Heartbeat failed: " + e.getMessage());
        }
    }

    private String buildKey(RedisRegistration registration) {
        // Key 格式: discovery:service-name:instance-id
        return properties.getPrefix() + ":" + registration.getServiceId() + ":" + registration.getInstanceId();
    }
}