package com.github.oxyzero.volt;

import com.github.oxyzero.volt.middleware.Middleware;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Consumer;

/**
 *
 * @author Renato Machado
 */
public abstract class Server {
    
    /**
     * Dependency Injection.
     */
    protected final Map<String, Object> dependencies;
    
    /**
     * Route middleware.
     */
    protected final Map<String, List<Middleware>> middlewares;
    
    /**
     * Server connected port.
     */
    protected int connectedPort;
    
    /**
     * If the server is active.
     */
    protected boolean active;

    protected Server() {
        this.dependencies = new HashMap<>();
        this.middlewares = new HashMap<>();
        this.active = false;
        this.connectedPort = -1;
    }

    /**
     * Boots the server.
     *
     * @param port The port number.
     */
    protected abstract void boot(int port);

    /**
     * Restarts the server.
     * 
     * @param port The port number.
     */
    protected abstract void restart(int port);
    
    /**
     * Streams the server in the given port.
     *
     * @param port The port number.
     */
    public abstract void stream(int port);
    
    public abstract void listen(String route, Connection action);

    public abstract void listen(String route, Consumer<Request> action);

    public abstract void forget(String route);
    
    public abstract void send(String route, String target, String message);
    
    public abstract void shutdown();
    
    /**
     * Returns if the server is currently working.
     *
     * @return True if the server is active, false otherwise.
     */
    public boolean isActive() {
        return this.active;
    }
    
    /**
     * Allows Volt to access the given dependency through a key.
     * 
     * @param key Dependency key.
     * @param dependency Dependency.
     */
    public void use(String key, Object dependency)
    {
        this.dependencies.put(key, dependency);
    }
    
    /**
     * Allows Volt to access the given objects through its keys.
     * 
     * @param dependencies Multidimensional array containing in each row a String and a Object.
     */
    public void use(Object[][] dependencies)
    {
        for (Object[] dependency : dependencies) {
            this.use((String) dependency[0], dependency[1]);
        }
    }
    
    /**
     * Returns the object from the given key.
     * 
     * @param key Dependency key.
     * @return Dependency or null if key does not exist.
     */
    public Object get(String key)
    {
        return this.dependencies.get(key);
    }
    
    /**
     * Creates a new middleware for a given route.
     * 
     * @param route Route that will trigger the middleware. If given a '*', the
     * middleware will be applied to every route.
     * @param middlewares Middleware to be executed.
     */
    public void middleware(String route, Middleware... middlewares)
    {
        synchronized (this.middlewares) {
            if (! this.middlewares.containsKey(route)) {
                this.middlewares.put(route, new ArrayList<>());
            }

            this.middlewares.get(route).addAll(Arrays.asList(middlewares));
        }
    }
    
    /**
     * Gets the middleware of a route.
     * 
     * @param route Route.
     * @return Routes middleware, or null if no route was found.
     */
    private List<Middleware> getRouteMiddleware(String route)
    {
        synchronized (this.middlewares) {
            if (this.middlewares.containsKey(route)) {
                return this.middlewares.get(route);
            }
        }
        
        return null;
    }
    
    /**
     * Executes all before middleware.
     * 
     * @param request Request data.
     */
    protected void executeBeforeMiddlewares(Request request)
    {
        synchronized (this.middlewares) {
            List<Middleware> middlewares = getRouteMiddleware("*");
            
            if (middlewares != null) {
                // Execute all of the wildcard middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, dependencies);
                }
            }
            
            middlewares = getRouteMiddleware(request.route());
            
            if (middlewares != null) {
                // Execute all the before middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, dependencies);
                }
            }
            
            middlewares = Volt.getRouteMiddlewares("*");

            if (middlewares != null) {
                // Execute all of the global wildcard middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, dependencies);
                }
            }

            middlewares = Volt.getRouteMiddlewares(request.route());

            if (middlewares != null) {
                // Execute all the before global middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, dependencies);
                }
            }
        }
    }
    
    /**
     * Executes all after middleware.
     *
     * @param request Request data.
     */
    protected void executeAfterMiddlewares(Request request) {
        synchronized (this.middlewares) {
            List<Middleware> middlewares = getRouteMiddleware("*");

            if (middlewares != null) {
                // Execute all of the wildcard middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, dependencies);
                }
            }

            middlewares = getRouteMiddleware(request.route());

            if (middlewares != null) {
                // Execute all the after middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, dependencies);
                }
            }
            
            middlewares = Volt.getRouteMiddlewares("*");

            if (middlewares != null) {
                // Execute all of the global wildcard middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, dependencies);
                }
            }

            middlewares = Volt.getRouteMiddlewares(request.route());

            if (middlewares != null) {
                // Execute all the after global middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, dependencies);
                }
            }
        }
    }
    
    /**
     * Gets the port that the server is currently using.
     * 
     * @return Returns the active connected port, if the server is not connected
     * returns -1.
     */
    public int getPort()
    {
        return this.connectedPort;
    }
    
    /**
     * Target the given IPv4 with the same port as this server.
     * 
     * @param target Target IPv4.
     * @return IPv4:Port
     */
    public String target(String target)
    {
        return target + ":" + this.getPort();
    }
    
    /**
     * Returns the signature of the server.
     *
     * @return IPv4 and Port (separated by ":")
     */
    public String signature() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return "0.0.0.0";
        }
    }
}
