package com.github.oxyzero.volt.middleware;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.Container;

import java.util.Map;

/**
 * This interface consists on building a contract to filter information during
 * the lifetime of a request by using simple hooks that get called in certain
 * points of Volt execution.
 * 
 * @author Renato Machado
 */
public interface Middleware {
    
    /**
     * Triggers before the execution of a request.
     * 
     * @param request The received request.
     * @param dependencies The server dependencies.
     */
    void before(Request request, Container container);
    
    /**
     * Triggers after the execution of a request.
     * 
     * @param request The received request.
     * @param dependencies The server dependencies.
     */
    void after(Request request, Container container);
}
