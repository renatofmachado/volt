package com.github.oxyzero.volt.support;

import java.util.function.Function;

/**
 * Provides a service for Volt to use.
 */
@FunctionalInterface
public interface ServiceProvider<R> extends Function<Container, R> {

    default R register(Container container) {
        return this.apply(container);
    }

}
