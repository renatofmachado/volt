package com.github.oxyzero.volt;

import com.github.oxyzero.volt.middleware.Middleware;
import com.github.oxyzero.volt.protocols.tcp.TcpServer;
import com.github.oxyzero.volt.protocols.udp.UdpServer;
import com.github.oxyzero.volt.support.Task;
import com.github.oxyzero.volt.support.TaskManager;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
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
     * Allows Volt to print stack traces and output debug information.
     * 
     * Note: This is not advised to be used in a production environment.
     */
    private static boolean DEBUG = false;
    
    /**
     * Print Stream to output debug information.
     */
    private static PrintStream output = null;
    
    /**
     * Volt global middleware.
     */
    private final static Map<String, List<Middleware>> middlewares = new HashMap<>();
    
    /**
     * Puts Volt in debug mode.
     * 
     * @param out Print Stream to be used.
     */
    public static void debug(PrintStream out)
    {
        DEBUG = true;
        output = out;
    }
    
    /**
     * Puts Volt in debug mode with the default print stream.
     */
    public static void debug()
    {
        debug(System.out);
    }
    
    /**
     * Prints a message with the current date in debug mode.
     * 
     * @param message Message.
     */
    private static void date(String message)
    {
        if (DEBUG) { 
            print(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + " - " + message); 
        }
    }
    
    /**
     * Prints a message in debug mode.
     * 
     * @param message Message.
     */
    private static void print(String message)
    {
        if (DEBUG) {
            output.println(message);
        }
    }
    
    /**
     * Provides a stack trace in debug mode.
     */
    private static void stacktrace()
    {
        if (DEBUG) {
            date("Volt: Start of Stack Trace");
            
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            
            for (int i = 2; i < stackTraceElements.length; i++) {
                print(stackTraceElements[i].toString());
            }
            
            print("Volt: End of Stack Trace");
        }
    }
    
    /**
     * Builds a new instance of the UDP server, or gets the current server
     * that is bound to that port.
     * 
     * @param port Port.
     * @return UdpServer instance or throws an exception.
     */
    public static UdpServer udp(final int port)
    {
        stacktrace();
        
        synchronized (instances) {
            if (instances.containsKey(port)) {
                
                date("Volt is currently using the port " + port + " on the server: " + instances.get(port).toString());
                
                if (instances.get(port) instanceof UdpServer) {
                    
                    date("The port " + port + " currently belongs to a UdpServer.");
                    
                    if (! instances.get(port).isActive()) {
                        date("The port " + port + ", with an instance of UdpServer is now going to be activated.");
                        
                        try {
                            new Thread() {
                                @Override
                                public void run() {
                                    instances.get(port).stream(port);
                                }
                            }.start();
                        } catch (IllegalArgumentException e) {
                            date("Volt could not open the port " + port + " to a UdpServer instance because: " + e.getMessage() + "\n\n");
                            throw e;
                        }
                    }
                    
                    date("The port " + port + " is currently active in a UdpServer instance: " + instances.get(port).toString() + "\n\n");
                    
                    return (UdpServer) instances.get(port);
                }
                
                throw new IllegalArgumentException("The given port is already bound to another service that is not a UDP Server.\n\n");
            }
            
            date("Volt is now reserving the port " + port + " to a instance of UdpServer.");
            
            final UdpServer server = new UdpServer();
            
            try {
                new Thread() {
                    @Override
                    public void run() {
                        server.stream(port);
                    }
                }.start();
            } catch (IllegalArgumentException e) {
                date("Volt could not open the port " + port + " to a UdpServer instance because: " + e.getMessage() + "\n\n");
                throw e;
            }
            
            date("Volt successfully reserved the port " + port + " to a UdpServer instance @ " + server.toString() + "\n\n");
            
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
                print("Volt was unable to use the port " + port + ". Trying with the fallback port: " + fallback);
                return Volt.udp(fallback);
            } catch (Exception fallbackException) {
                print("Volt was unable to use the fallback port " + fallback);
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
        stacktrace();
        
        synchronized (instances) {
            if (instances.containsKey(port)) {
                
                date("Volt is currently using the port " + port + " on the server: " + instances.get(port).toString());
                
                if (instances.get(port) instanceof TcpServer) {
                    
                    date("The port " + port + " currently belongs to a TcpServer.");
                    
                    if (! instances.get(port).isActive()) {
                        date("The port " + port + ", with an instance of TcpServer is now going to be activated.");

                        try {
                            new Thread() {
                                @Override
                                public void run() {
                                    instances.get(port).stream(port);
                                }
                            }.start();
                        } catch (IllegalArgumentException e) {
                            date("Volt could not open the port " + port + " to a UdpServer instance because: " + e.getMessage() + "\n\n");
                            throw e;
                        }
                    }
                    
                    date("The port " + port + " is currently active in a TcpServer instance: " + instances.get(port).toString() + "\n\n");
                    
                    return (TcpServer) instances.get(port);
                }
                
                throw new IllegalArgumentException("The given port is already bound to another service that is not a TCP Server.");
            }
            
            date("Volt is now reserving the port " + port + " to a instance of TcpServer.");
            
            final TcpServer server = new TcpServer();

            try {
                new Thread() {
                    @Override
                    public void run() {
                        server.stream(port);
                    }
                }.start();
            } catch (IllegalArgumentException e) {
                date("Volt could not open the port " + port + " to a TcpServer instance because: " + e.getMessage() + "\n\n");
                throw e;
            }
            
            date("Volt successfully reserved the port " + port + " to a TcpServer instance @ " + server.toString() + "\n\n");
            
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
                print("Volt was unable to use the port " + port + ". Trying with the fallback port: " + fallback);
                return Volt.tcp(fallback);
            } catch (Exception fallbackException) {
                print("Volt was unable to use the fallback port " + fallback);
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
        stacktrace();
        
        print("Volt is trying to stop the port " + port);
        
        synchronized (instances) {
            if (! instances.containsKey(port)) {
                print("Volt isn't aware of port " + port);
                throw new IllegalArgumentException("Volt does not know about any service running on port " + port);
            }
            
            print("Volt is stopping port " + port + " of the server: " + instances.get(port).toString());
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
            print("Volt is removing the port " + port + " of the server: " + instances.get(port).toString());
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
