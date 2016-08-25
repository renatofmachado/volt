package com.github.oxyzero.volt;

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
     * The hostname of the requester.
     */
    private String hostname;

    public Requester(Map<String, Object> args) {
        this.target = (String) args.get("target");
        this.hostname = (String) args.get("hostname");
    }

    public String id() {
        // TODO: Retrieve a unique identification of the requester. Maybe make a combo of the hostname + ip?
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
     * Returns the target IPv4:Port.
     * Should only be used for client-side actions.
     *
     * @return Target IPv4:Port or null if not available.
     */
    public String target()
    {
        return this.target;
    }
}
