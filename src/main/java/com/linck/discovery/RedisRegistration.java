package com.linck.discovery;

import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RedisRegistration implements Registration {
    private String serviceId;
    private String instanceId;
    private String host;
    private int port;
    private Map<String, String> metadata = new HashMap<>();
    private URI uri; // 缓存一下 URI

    // 必须有无参构造供 Jackson 反序列化
    public RedisRegistration() {}

    public RedisRegistration(String serviceId, String instanceId, String host, int port, Map<String, String> metadata) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.metadata = metadata;
        this.uri = URI.create("http://" + host + ":" + port);
    }

    @Override
    public String getServiceId() { return serviceId; }
    @Override
    public String getHost() { return host; }
    @Override
    public int getPort() { return port; }
    @Override
    public boolean isSecure() { return false; } // 如果是 https 返回 true
    @Override
    public URI getUri() { return uri; }
    @Override
    public Map<String, String> getMetadata() { return metadata; }

    public String getInstanceId() { return instanceId; }
}