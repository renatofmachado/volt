package com.github.oxyzero.volt.middleware;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.Container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observer;

/**
 * This channel allows to notify every observer of the messages sent.
 * 
 * @author Renato Machado
 */
public class MessageSentMiddleware implements Middleware {
    
    /**
     * Where the message is being sent from.
     */
    private final String from;
    
    /**
     * The observer that wants to know about the sent messages.
     */
    private final List<Observer> observers;
    
    public MessageSentMiddleware(String from, Observer... observers)
    {
        this.from = from;
        
        this.observers = new ArrayList<>();
        this.observers.addAll(Arrays.asList(observers));
    }

    @Override
    public void before(Request request, Container container) {}

    @Override
    public void after(Request request, Container container) {
        String message = this.from + request.route() + ": " + request.message();
        
        for (Observer observer : this.observers) {
            observer.update(null, message);
        }
    }
    
}
