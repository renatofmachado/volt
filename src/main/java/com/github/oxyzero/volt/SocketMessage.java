package com.github.oxyzero.volt;

import java.util.HashMap;
import java.util.Map;

public abstract class SocketMessage {

    protected String message;

    protected Map<String, String> headers;

    public SocketMessage() {
        this.headers = new HashMap<>();
    }

    public abstract void defineMessage();

    public abstract void defineHeaders();

    public String message() {
        return this.message;
    }

    public Map<String, String> headers() {
        return this.headers;
    }

    public String header(String key) {
        return this.headers.get(key);
    }
}
