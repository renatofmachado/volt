package com.github.oxyzero.volt.middleware;

import com.github.oxyzero.volt.Request;

import java.util.*;

/**
 * This middleware allows to notify every observer of the messages sent.
 * 
 * @author Renato Machado
 */
public class MessageReceivedMiddleware implements Middleware {

    /**
     * Where the message is being received from.
     */
    private final String from;

    /**
     * The observer that wants to know about the received messages.
     */
    private final List<Observer> observers;

    public MessageReceivedMiddleware(String from, Observer... observers) {
        this.from = from;

        this.observers = Arrays.asList(observers);
    }

    @Override
    public void before(Request request, Map<String, Object> dependencies) {}

    @Override
    public void after(Request request, Map<String, Object> dependencies) {
        String message = this.from + request.route() + ": " + request.message();

        for (Observer observer : this.observers) {
            observer.update(null, message);
        }
    }
    
}
