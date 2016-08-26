package com.github.oxyzero.volt;

import com.github.oxyzero.volt.middleware.Middleware;
import com.github.oxyzero.volt.support.Container;
import com.github.oxyzero.volt.support.ServiceProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.function.Consumer;

/**
 *
 * @author Renato Machado
 */
public abstract class Server {
    
    /**
     * Dependency Injection.
     */
    protected final Container services;
    
    /**
     * Route middleware.
     */
    protected final Map<String, List<Middleware>> middlewares;

    /**
     * Router.
     */
    private final Router router;

    /**
     * Server connected port.
     */
    protected int connectedPort;

    //private Socket<T> socket;
    
    /**
     * If the server is active.
     */
    protected boolean active;

    protected Server() {
        this.services = new Container();
        this.middlewares = new HashMap<>();
        this.closeServerState();
        this.router = new Router();
    }

    protected abstract Socket<?> socket();

    /**
     * Boots the server.
     *
     * @param port The port number.
     */
    protected void boot(int port)
    {
        this.socket().boot(port);

        this.active = true;

        if (port == 0 || this.connectedPort == -1) {
            this.connectedPort = this.socket().getLocalPort();
        } else {
            this.connectedPort = port;
        }
    }

    /**
     * Restarts the server.
     * 
     * @param port The port number.
     */
    private void restart(int port)
    {
        this.closeServerState();
        this.socket().restart(port);
    }

    /**
     * Shuts the server down.
     */
    public synchronized void shutdown()
    {
        this.socket().shutdown();
        this.closeServerState();
    }

    /**
     * Changes the state of the server to closed.
     */
    private void closeServerState()
    {
        this.active = false;
        this.connectedPort = -1;
    }

    /**
     * Streams the server in the given port.
     *
     * @param port The port number.
     */
    public abstract void stream(int port);

    public abstract void listen(String route, Connection action);

    public void listen(String route, Consumer<Request> action) {
        Connection connection = new Connection() {
            @Override
            public void run(Request request) {
                action.accept(request);
            }
        };

        this.listen(route, connection);
    }

    /**
     * Forgets a route.
     *
     * @param route Route identifier.
     */
    public void forget(String route) {
        this.router().remove(route);
    }
    
    public abstract void send(String route, String target, String message, String... headers);
    
    /**
     * Returns if the server is currently working.
     *
     * @return True if the server is active, false otherwise.
     */
    public boolean isActive() {
        return this.active;
    }
    
    /**
     * Registers a service that Volt can use.
     * 
     * @param key Service key.
     * @param provider Service Provider.
     */
    public void register(String key, ServiceProvider<Object> provider)
    {
        this.register(key, provider, false);
    }

    /**
     * Registers a service that Volt can use.
     *
     * @param key Service key.
     * @param provider Service Provider.
     * @param singleton If the service is a singleton.
     */
    public void register(String key, ServiceProvider<Object> provider, boolean singleton) {
        if (singleton) {
            this.services.singleton(key, provider);
        } else {
            this.services.register(key, provider);
        }
    }


    /**
     * Resolves the given key from the service container.
     * 
     * @param key Service key.
     * @return Service result.
     */
    public Object resolve(String key)
    {
        return this.services.resolve(key);
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
                    middleware.before(request, services);
                }
            }
            
            middlewares = getRouteMiddleware(request.route());
            
            if (middlewares != null) {
                // Execute all the before middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, services);
                }
            }
            
            middlewares = Volt.getRouteMiddlewares("*");

            if (middlewares != null) {
                // Execute all of the global wildcard middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, services);
                }
            }

            middlewares = Volt.getRouteMiddlewares(request.route());

            if (middlewares != null) {
                // Execute all the before global middleware.
                for (Middleware middleware : middlewares) {
                    middleware.before(request, services);
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
                    middleware.after(request, services);
                }
            }

            middlewares = getRouteMiddleware(request.route());

            if (middlewares != null) {
                // Execute all the after middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, services);
                }
            }
            
            middlewares = Volt.getRouteMiddlewares("*");

            if (middlewares != null) {
                // Execute all of the global wildcard middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, services);
                }
            }

            middlewares = Volt.getRouteMiddlewares(request.route());

            if (middlewares != null) {
                // Execute all the after global middleware.
                for (Middleware middleware : middlewares) {
                    middleware.after(request, services);
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
     * Gets the server router.
     *
     * @return Router.
     */
    public Router router()
    {
        return this.router;
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
}
