package com.github.oxyzero.volt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SocketMessage<T> {

    protected String message;

    protected Map<String, String> headers;

    public SocketMessage() {
        this.headers = new HashMap<>();
    }

    public abstract String getHeaderDelimiter();
    public abstract String getHeaderDelimiterRegex();
    public abstract String getMessageDelimiter();
    public abstract String getMessageDelimiterRegex();
    public abstract String getSeparator();
    public abstract String getSeparatorRegex();

    public abstract void toReceive(T receiver);

    public abstract List<String> toSend(Request request);

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
