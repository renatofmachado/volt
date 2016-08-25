package com.github.oxyzero.volt.support;

import com.github.oxyzero.volt.Request;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBuilder {

    private final Map<String, Object> arguments;

    private final Map<String, List<String>> variables;

    public RequestBuilder() {
        this.arguments = new HashMap<>();
        this.variables = new HashMap<>();
    }

    public void message(String message)
    {
        this.arguments.put("message", message);
        this.arguments.put("length", message.length());
    }

    public void route(String route)
    {
        this.arguments.put("route", route);
    }

    public void target(String target) {
        this.arguments.put("target", target);
    }

    public void socket(Socket socket)
    {
        this.arguments.put("socket", socket);
    }

    public void put(String key, List<String> value) {
        this.variables.put(key, value);
    }

    public Request build()
    {
        return new Request(this.arguments, this.variables);
    }

}
