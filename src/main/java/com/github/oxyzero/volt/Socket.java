package com.github.oxyzero.volt;

public interface Socket<T> {

    T boot(Object... args);

    T restart(Object... args);

    void shutdown();

    T server();

    int getPort();

    int getLocalPort();

    Object receive() throws Exception;

    void send(Request request);
}
