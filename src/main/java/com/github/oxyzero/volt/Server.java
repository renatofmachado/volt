package com.github.oxyzero.volt;

import com.github.oxyzero.volt.channels.Channel;

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
     * Route channels.
     */
    protected final Map<String, List<Channel>> channels;
    
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
        this.channels = new HashMap<>();
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
     * Creates a new channel for a given route.
     * 
     * @param route Route that will trigger the channel. If given a '*', the
     * channels will be applied to every route.
     * @param channels Channels to be executed.
     */
    public void channel(String route, Channel... channels)
    {
        synchronized (this.channels) {
            if (! this.channels.containsKey(route)) {
                this.channels.put(route, new ArrayList<>());
            }

            this.channels.get(route).addAll(Arrays.asList(channels));
        }
    }
    
    /**
     * Gets the channels of a route.
     * 
     * @param route Route.
     * @return Routes channels, or null if no route was found.
     */
    public List<Channel> getRouteChannels(String route) 
    {
        synchronized (this.channels) {
            if (this.channels.containsKey(route)) {
                return this.channels.get(route);
            }
        }
        
        return null;
    }
    
    /**
     * Executes all before channels.
     * 
     * @param request Request data.
     */
    protected void executeBeforeChannels(Request request)
    {
        synchronized (this.channels) {
            List<Channel> channels = getRouteChannels("*");
            
            if (channels != null) {
                // Execute all of the wildcard channels.
                for (Channel channel : channels) {
                    channel.before(request, dependencies);
                }
            }
            
            channels = getRouteChannels(request.route());
            
            if (channels != null) {
                // Execute all the before channels.
                for (Channel channel : channels) {
                    channel.before(request, dependencies);
                }
            }
            
            channels = Volt.getRouteChannels("*");

            if (channels != null) {
                // Execute all of the global wildcard channels.
                for (Channel channel : channels) {
                    channel.before(request, dependencies);
                }
            }

            channels = Volt.getRouteChannels(request.route());

            if (channels != null) {
                // Execute all the before global channels.
                for (Channel channel : channels) {
                    channel.before(request, dependencies);
                }
            }
        }
    }
    
    /**
     * Executes all after channels.
     *
     * @param request Request data.
     */
    protected void executeAfterChannels(Request request) {
        synchronized (this.channels) {
            List<Channel> channels = getRouteChannels("*");

            if (channels != null) {
                // Execute all of the wildcard channels.
                for (Channel channel : channels) {
                    channel.after(request, dependencies);
                }
            }

            channels = getRouteChannels(request.route());

            if (channels != null) {
                // Execute all the after channels.
                for (Channel channel : channels) {
                    channel.after(request, dependencies);
                }
            }
            
            channels = Volt.getRouteChannels("*");

            if (channels != null) {
                // Execute all of the global wildcard channels.
                for (Channel channel : channels) {
                    channel.after(request, dependencies);
                }
            }

            channels = Volt.getRouteChannels(request.route());

            if (channels != null) {
                // Execute all the after global channels.
                for (Channel channel : channels) {
                    channel.after(request, dependencies);
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
