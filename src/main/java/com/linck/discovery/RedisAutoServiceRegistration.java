package com.linck.discovery;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;

public class RedisAutoServiceRegistration extends AbstractAutoServiceRegistration<RedisRegistration> {

    private final RedisDiscoveryProperties properties;
    private final RedisRegistration registration;

    public RedisAutoServiceRegistration(ApplicationContext context,
                                        ServiceRegistry<RedisRegistration> serviceRegistry,
                                        RedisDiscoveryProperties properties,
                                        RedisRegistration registration) {
        super(context, serviceRegistry, new AutoServiceRegistrationProperties());
        this.properties = properties;
        this.registration = registration;
    }

    @Override
    protected RedisRegistration getRegistration() {
        return this.registration;
    }

    @Override
    protected RedisRegistration getManagementRegistration() {
        return null;
    }

    @Override
    protected Object getConfiguration() {
        return this.properties;
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }
}