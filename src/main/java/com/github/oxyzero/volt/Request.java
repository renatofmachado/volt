package com.github.oxyzero.volt;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * A class that handles the request received by a communications protocol.
 * 
 * @author Renato Machado
 */
public class Request {
    
    /**
     * The arguments to build up the request.
     */
    private final Map<String, Object> args;
    
    public Request(Map<String, Object> args) 
    {
        this.args = args;
    }
    
    /**
     * Puts a new message on the "message" argument.
     * 
     * @param message New message.
     */
    public void message(String message)
    {
        this.args.put("volt-message", message);
    }
    
    /**
     * Returns the request message.
     * 
     * @return Message received in the request.
     */
    public String message()
    {
        return (String) this.args.get("volt-message");
    }
    
    /**
     * Returns the length of the original received message.
     * 
     * @return Message length.
     */
    public int length()
    {
        return (Integer) this.args.get("volt-length");
    }
    
    /**
     * Puts a new from on the "from" argument.
     *
     * @param from New from.
     */
    public void from(String from) {
        this.args.put("volt-from", from);
    }
    
    /**
     * Gets the IPv4 that sent the request.
     * 
     * @return The IPv4 of the requester.
     */
    public String from()
    {
        return ((String) this.args.get("volt-from")).split(":")[0];
    }
    
    /**
     * Puts a new port on the "port" argument.
     *
     * @param port New port.
     */
    public void port(int port) {
        this.args.put("volt-port", port);
    }
    
    /**
     * Gets the port that sent the request.
     *
     * @return The port of the requester.
     */
    public int port() {
        return Integer.valueOf(((String) this.args.get("volt-from")).split(":")[1]);
    }
    
    /**
     * Returns the IPv4 and port of the requester, separated by a ':'.
     * 
     * @return IPv4 and port of the requester.
     */
    public String requester()
    {
        return (String) this.args.get("volt-from");
    }
    
    /**
     * Puts a new hostname on the "hostname" argument.
     *
     * @param hostname New hostname.
     */
    public void hostname(String hostname) {
        this.args.put("volt-hostname", hostname);
    }
    
    /**
     * Gets the hostname that is associated to the requester address.
     * 
     * @return Hostname based on the requester address.
     */
    public String hostname()
    {
        return (String) this.args.get("volt-hostname");
    }
    
    /**
     * Gets the targeted route.
     * 
     * @return Route that the requester targeted.
     */
    public String route()
    {
        return (String) this.args.get("volt-route");
    }
    
    /**
     * Puts a new address on the "address" argument.
     *
     * @param address New address.
     */
    public void address(InetAddress address) {
        this.args.put("volt-address", address);
    }
    
    /**
     * Returns the InetAddress of the requester.
     * 
     * @return InetAddress of the requester.
     */
    public InetAddress address()
    {
        return (InetAddress) this.args.get("volt-address");
    }
    
    /**
     * Puts a new target on the "target" argument.
     *
     * @param target New target.
     */
    public void target(String target) {
        this.args.put("volt-target", target);
    }
    
    /**
     * Returns the target IPv4:Port.
     * Should only be used for client-side actions.
     * 
     * @return Target IPv4:Port or null if not available.
     */
    public String target()
    {
        return (String) this.args.get("volt-target");
    }
    
    /**
     * Returns the number of total packets that were received.
     * This method should be used for UDP requests only.
     * 
     * @return Number of total packets received. -1 if the request is not a UDP request.
     */
    public int packets()
    {
        if (this.args.containsKey("volt-packets")) {
            return Integer.parseInt((String) this.args.get("volt-packets"));
        }
        
        return -1;
    }
    
    /**
     * Returns the TCP socket. This method should be used for
     * TCP requests only.
     *
     * @return TCP Socket.
     */
    public Socket socket()
    {
        return (Socket) this.args.get("volt-socket");
    }
    
    /**
     * Returns the input stream from the socket. This method
     * should be used for TCP requests only.
     *
     * @return Input Stream from the socket.
     */
    public BufferedReader input()
    {
        return (BufferedReader) this.args.get("volt-input");
    }
    
    /**
     * Returns the output stream from the socket. This method should be used for
     * TCP requests only.
     *
     * @return Output Stream from the socket.
     */
    public PrintWriter output()
    {
        return (PrintWriter) this.args.get("volt-output");
    }
    
    /**
     * Puts a list of values into a variable.
     *
     * @param variable Variable name.
     * @param values Variable values.
     */
    public void put(String variable, List<String> values) {
        this.args.put(variable, values);
    }
    
    /**
     * Gets the list of variables received in UDP.
     * Example: 
     * 
     * On a route such as ":username|:password"
     * Doing request.get("username") would return all usernames.
     * Doing request.get("password") would return all passwords.
     * 
     * @param variable Variable name identified in the route (starts with :)
     * @return List of values from the variable name.
     */
    public List<String> get(String variable)
    {
        return (List<String>) this.args.get(variable);
    }
    
    /**
     * Checks if the requester is the same as the current user.
     * This is useful to block broadcast signals.
     * 
     * @return True if the requester is the same as the current user, false otherwise.
     */
    public boolean same()
    {
        String requester = this.from();
        
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    
                    if (requester.equals(i.getHostAddress())) {
                        return true;
                    }
                }
            }
        } catch (SocketException ignored) {}
        
        return false;
    }
}
