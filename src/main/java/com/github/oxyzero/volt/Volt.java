package com.github.oxyzero.volt;

import com.github.oxyzero.volt.middleware.Middleware;
import com.github.oxyzero.volt.protocols.tcp.TcpServer;
import com.github.oxyzero.volt.protocols.udp.UdpServer;
import com.github.oxyzero.volt.support.ClientFactory;
import com.github.oxyzero.volt.support.ServerFactory;
import com.github.oxyzero.volt.support.Task;
import com.github.oxyzero.volt.support.TaskManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Volt allows to manage servers and extend them in the lifetime of a application.
 * 
 * @author Renato Machado
 */
public class Volt {
    
    /**
     * Current active Volt services.
     */ 
    private static final Map<Integer, Server> instances = new HashMap<>();

    /**
     * Volt global middleware.
     */
    private final static Map<String, List<Middleware>> middlewares = new HashMap<>();

    /**
     * Generates a server based on its protocol.
     *
     * If the server was already instantiated, it returns the server.
     *
     * @param protocol Server protocol.
     * @param port Server port.
     * @return Server
     */
    public static Server server(String protocol, int port)
    {
        if (port < 0) {
            throw new IllegalArgumentException("Invalid port was given.");
        }

        if (instances.containsKey(port)) {
            Server server = instances.get(port);

            if (server instanceof UdpServer && protocol.equalsIgnoreCase("udp")) {
                return server;
            }

            if (server instanceof TcpServer && protocol.equalsIgnoreCase("tcp")) {
                return server;
            }

            throw new IllegalArgumentException("The port " + port + " is associated with another protocol.");
        }

        Server server = new ServerFactory().make(protocol, port);

        instances.put(port, server);

        return server;
    }

    /**
     * Generates a client based on its protocol.
     *
     * @param protocol Client Protocol.
     * @param port Client port.
     * @return Client
     */
    public static Client client(String protocol, int port) {
        return new ClientFactory().make(protocol, port);
    }

    /**
     * Generates a client based on its protocol.
     * It will use a dynamic port.
     *
     * @param protocol Client Protocol.
     * @return Client
     */
    public static Client client(String protocol) {
        return Volt.client(protocol, 0);
    }
    
    /**
     * Stops a service on the given port, but maintains it's instance stored.
     * If no service exists on the given port, it throws an exception.
     * 
     * @param port Service port.
     */
    public static void stop(int port)
    {
        synchronized (instances) {
            if (! instances.containsKey(port)) {
                throw new IllegalArgumentException("Volt does not know about any service running on port " + port);
            }
            
            instances.get(port).shutdown();
        }
    }
    
    /**
     * Kills a service on the given port, and deletes it's instance. 
     * If no service exists on the given port, it throws an exception.
     *
     * @param port Service port.
     */
    public static void kill(int port)
    {
        Volt.stop(port);
        
        synchronized (instances) {
            instances.remove(port);
        }
    }

    /**
     * Kills a service on the given port, and deletes it's instance
     * after the given period of time in seconds.
     * If no service exists on the given port, it throws an exception.
     *
     * @param port Service port.
     * @param after Number of seconds to kill the service.
     */
    public static void kill(int port, int after)
    {
        TaskManager tm = new TaskManager();

        tm.after(after).once(new Task() {
            @Override
            public void fire() {
                Volt.kill(port);
                tm.destroy();
            }
        });
    }
    
    /**
     * Creates a new global middleware for a given route.
     *
     * @param route Route that will trigger the middleware. If given a '*', the
     * middleware will be applied to every route.
     * @param middlewares Middlewares to be executed.
     */
    public static void middleware(String route, Middleware... middlewares)
    {
        synchronized (Volt.middlewares) {
            if (!Volt.middlewares.containsKey(route)) {
                Volt.middlewares.put(route, new ArrayList<>());
            }

            Volt.middlewares.get(route).addAll(Arrays.asList(middlewares));
        }
    }
    
    /**
     * Drops all the global middleware of a given route.
     * 
     * @param route Route that have middleware associated.
     */
    public static void dropMiddlewares(String route)
    {
        if (Volt.middlewares.containsKey(route)) {
            Volt.middlewares.remove(route);
        }
    }
    
    /**
     * Gets the global middleware of Volt.
     *
     * @param route Route.
     * @return Route middleware, or null if no route was found.
     */
    public static List<Middleware> getRouteMiddlewares(String route) {
        synchronized (Volt.middlewares) {
            if (Volt.middlewares.containsKey(route)) {
                return Volt.middlewares.get(route);
            }
        }

        return null;
    }
    
    /**
     * Returns the current localhost IPv4.
     * 
     * @return Returns the localhost IPv4.
     */
    public static String localhost()
    {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return "0.0.0.0";
        }
    }
    
    /**
     * Returns the current localhost IPv4 with the given port separated by ":".
     * 
     * @param port Service port.
     * @return IPv4:Port.
     */
    public static String localhost(int port)
    {
        return Volt.localhost() + ":" + String.valueOf(port);
    }
}
