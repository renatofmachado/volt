package com.github.oxyzero.volt;

import java.net.InetAddress;
import java.util.Map;

/**
 * This class represents a requester in a connection.
 * 
 * @author Renato Machado
 */
public class Requester {

    /**
     * The IPv4:Port of the requester.
     */
    private String target;

    /**
     * The address of the requester.
     */
    private InetAddress address;

    /**
     * The hostname of the requester.
     */
    private String hostname;

    public Requester(String target, InetAddress address) {
        this.target = target;
        this.address = address;
    }

    public Requester(Map<String, Object> args) {
        this.target = (String) args.get("volt-target");
        this.address = (InetAddress) args.get("volt-address");
        this.hostname = (String) args.get("volt-hostname");
    }

    public String id() {
        return null;
    }

    /**
     * Gets the IPv4 that sent the request.
     *
     * @return The IPv4 of the requester.
     */
    public String from()
    {
        return this.target.split(":")[0];
    }

    /**
     * Gets the port that sent the request.
     *
     * @return The port of the requester.
     */
    public int port() {
        return Integer.valueOf(target.split(":")[1]);
    }

    /**
     * Returns the InetAddress of the requester.
     *
     * @return InetAddress of the requester.
     */
    public InetAddress address()
    {
        return this.address;
    }

    /**
     * Returns the target IPv4:Port.
     * Should only be used for client-side actions.
     *
     * @return Target IPv4:Port or null if not available.
     */
    public String target()
    {
        return this.target;
    }


    /**
     * Gets the hostname that is associated to the requester address.
     *
     * @return Hostname based on the requester address.
     */
    public String hostname()
    {
        return this.hostname;
    }
}
