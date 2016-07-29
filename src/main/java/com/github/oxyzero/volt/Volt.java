package com.github.oxyzero.volt;

import com.github.oxyzero.volt.middleware.Middleware;
import com.github.oxyzero.volt.protocols.tcp.TcpServer;
import com.github.oxyzero.volt.protocols.udp.UdpServer;
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
     * Builds a new instance of the UDP server, or gets the current server
     * that is bound to that port.
     * 
     * @param port Port.
     * @return UdpServer instance or throws an exception.
     */
    public static UdpServer udp(final int port)
    {
        synchronized (instances) {
            if (instances.containsKey(port)) {
                
                if (instances.get(port) instanceof UdpServer) {
                    
                    if (! instances.get(port).isActive()) {
                        try {
                            new Thread() {
                                @Override
                                public void run() {
                                    instances.get(port).stream(port);
                                }
                            }.start();
                        } catch (IllegalArgumentException e) {
                            throw e;
                        }
                    }

                    return (UdpServer) instances.get(port);
                }
                
                throw new IllegalArgumentException("The given port is already bound to another service that is not a UDP Server.\n\n");
            }

            final UdpServer server = new UdpServer();
            
            try {
                new Thread() {
                    @Override
                    public void run() {
                        server.stream(port);
                    }
                }.start();
            } catch (IllegalArgumentException e) {
                throw e;
            }
            
            instances.put(port, server);
            
            return server;
        }
    }
    
    /**
     * Builds a new instance of the UDP server, or gets the current server that
     * is bound to that port. If unable to get a server on the given port, it
     * uses the fallback port.
     *
     * @param port Port.
     * @param fallback Fallback port.
     * @return UdpServer instance or throws an exception.
     */
    public static UdpServer udp(int port, int fallback)
    {
        try {
            return Volt.udp(port);
        } catch (Exception portException) {
            try {
                return Volt.udp(fallback);
            } catch (Exception fallbackException) {
                throw new IllegalArgumentException(fallbackException.getMessage());
            }
        }
    }
    
    /**
     * Builds a new instance of the TCP server, or gets the current server that
     * is bound to that port.
     *
     * @param port Port.
     * @return TcpServer instance or throws an exception.
     */
    public static TcpServer tcp(final int port)
    {
        synchronized (instances) {
            if (instances.containsKey(port)) {
                
                if (instances.get(port) instanceof TcpServer) {
                    
                    if (! instances.get(port).isActive()) {
                        try {
                            new Thread() {
                                @Override
                                public void run() {
                                    instances.get(port).stream(port);
                                }
                            }.start();
                        } catch (IllegalArgumentException e) {
                            throw e;
                        }
                    }
                    
                    return (TcpServer) instances.get(port);
                }
                
                throw new IllegalArgumentException("The given port is already bound to another service that is not a TCP Server.");
            }
            
            final TcpServer server = new TcpServer();

            try {
                new Thread() {
                    @Override
                    public void run() {
                        server.stream(port);
                    }
                }.start();
            } catch (IllegalArgumentException e) {
                throw e;
            }
            
            instances.put(port, server);

            return server;
        }
    }
    
    /**
     * Builds a new instance of the TCP server, or gets the current server that
     * is bound to that port. If unable to get a server on the given port, it
     * uses the fallback port.
     *
     * @param port Port.
     * @param fallback Fallback port.
     * @return TcpServer instance or throws an exception.
     */
    public static TcpServer tcp(int port, int fallback)
    {
        try {
            return Volt.tcp(port);
        } catch (Exception portException) {
            try {
                return Volt.tcp(fallback);
            } catch (Exception fallbackException) {
                throw new IllegalArgumentException(fallbackException.getMessage());
            }
        }
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
