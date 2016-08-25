package com.github.oxyzero.volt;

import com.github.oxyzero.volt.support.Task;
import com.github.oxyzero.volt.support.TaskManager;

import java.util.function.Consumer;

/**
 * This class represents a client that allows to quickly send a message to a
 * Volt server instance.
 *
 * @author Renato Machado
 */
public abstract class Client extends TaskManager {
    
    /**
     * Client server.
     */
    private final Server client;
    
    /**
     * Client port.
     */
    private final int port;
    
    /**
     * Creates a new Client.
     * 
     * @param client Client instance.
     * @param port Network port.
     */
    public Client(Server client, int port)
    {
        if (port < 0 || port > 49151) {
            throw new IllegalArgumentException("Invalid port was defined. Please select a valid port.");
        }
        
        this.client = client;
        this.port = port;
    }
    
    /**
     * Gets the client.
     *
     * @return Client instance.
     */
    public Server client() {
        return this.client;
    }
    
    /**
     * Sends a message, interpreted by a given route, for a target.
     * 
     * @param route Route the message is targeted for.
     * @param target IPv4:Port of the target.
     * @param message Message.
     */
    public Client send(String route, String target, String message) {

        Task request = new Task() {
            @Override
            public void fire() {
                client.send(route, target, message);
            }
        };

        super.fire(request);

        return this;
    }

    public abstract Client send(String headers, String target, Connection connection);

    public abstract Client send(String headers, String target, Consumer<Request> action);

    public abstract Client reply(Request request, Object response);

    /**
     * Returns the target IPv4:Port.
     * 
     * @param ip IP address.
     * @param port Service port.
     * @return IPv4:Port.
     */
    public String target(String ip, String port)
    {
        return this.target(ip, Integer.valueOf(port));
    }
    
    /**
     * Returns the target IPv4:Port.
     *
     * @param ip IP address.
     * @param port Service port.
     * @return IPv4:Port.
     */
    public String target(String ip, int port)
    {
        return ip + ":" + port;
    }

    @Override
    public Client after(int seconds) {
        super.after(seconds);

        return this;
    }

    @Override
    public Client every(int seconds) {
        super.every(seconds);

        return this;
    }

}
