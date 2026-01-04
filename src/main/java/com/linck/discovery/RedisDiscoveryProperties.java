package com.linck.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.redis.discovery")
public class RedisDiscoveryProperties {
    // Redis key 前缀
    private String prefix = "discovery";
    // 服务在 Redis 的过期时间 (秒)，默认 30s
    private long serverExpire = 30;
    // 客户端心跳上报频率 (秒)，默认 10s
    private long heartbeatInterval = 10;
    // 客户端本地缓存过期时间 (秒)，默认 15s
    private long cacheExpire = 15;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getServerExpire() {
        return serverExpire;
    }

    public void setServerExpire(long serverExpire) {
        this.serverExpire = serverExpire;
    }

    public long getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public long getCacheExpire() {
        return cacheExpire;
    }

    public void setCacheExpire(long cacheExpire) {
        this.cacheExpire = cacheExpire;
    }
}