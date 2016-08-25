package com.github.oxyzero.volt.support;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple container implementation for Volt dependency injection.
 */
public class Container {

    /**
     * Singleton services.
     */
    private final Map<String, Object> singletons;

    /**
     * Dependencies container.
     */
    private final Map<String, ServiceProvider<?>> services;

    public Container() {
        this.services = new HashMap<>();
        this.singletons = new HashMap<>();
    }

    /**
     * Resolves an service.
     *
     * @param key Service key.
     * @return Object
     */
    public Object resolve(String key) {
        if (this.singletons.containsKey(key)) {
            return this.singletons.get(key);
        }

        if (! this.services.containsKey(key)) {
            throw new IllegalArgumentException("The required object is not registered in Volt service container.");
        }

        return this.services.get(key).register(this);
    }

    /**
     * Declares a service provider as a singleton.
     *
     * @param key Service key.
     * @param provider Service Provider.
     */
    public void singleton(String key, ServiceProvider<Object> provider) {
        this.singletons.put(key, provider.register(this));
    }

    /**
     * Registers a service provider to the container.
     *
     * @param key Service key.
     * @param provider Service Provider.
     */
    public void register(String key, ServiceProvider<Object> provider) {
        this.services.put(key, provider);
    }

    /**
     * Removes a service from the container.
     *
     * @param key Service key.
     */
    public void remove(String key) {
        this.singletons.remove(key);
        this.services.remove(key);
    }

    /**
     * Checks if the container has the given key.
     *
     * @param key Service key.
     * @return True if the service exists in the container, false otherwise.
     */
    public boolean has(String key) {
        return this.singletons.containsKey(key) || this.services.containsKey(key);
    }
}
