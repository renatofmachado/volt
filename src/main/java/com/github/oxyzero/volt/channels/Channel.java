package com.github.oxyzero.volt.channels;

import com.github.oxyzero.volt.Request;

import java.util.Map;

/**
 * This class represents a Channel which consists of filtering information during
 * the lifetime of a request by using simple hooks that get called in certain
 * points of Volt execution. This allows to create powerful extensions as
 * well as easily manipulate the information that goes inside Volt.
 * 
 * @author Renato Machado
 */
public interface Channel {
    
    /**
     * Triggers before the execution of a request.
     * 
     * @param request The received request.
     * @param dependencies The server dependencies.
     */
    void before(Request request, Map<String, Object> dependencies);
    
    /**
     * Triggers after the execution of a request.
     * 
     * @param request The received request.
     * @param dependencies The server dependencies.
     */
    void after(Request request, Map<String, Object> dependencies);
}
