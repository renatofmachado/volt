package com.github.oxyzero.volt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * This class interacts with the end-user and provides information about the received request.
 */
public class Request {
    
    /**
     * The arguments to build up the request.
     */
    private final Map<String, Object> args;

    /**
     * Variables that can be attached to the request.
     */
    private final Map<String, List<String>> variables;

    /**
     * Requester.
     */
    private final Requester requester;

    public Request(Map<String, Object> args, Map<String, List<String>> variables)
    {
        this.args = args;
        this.variables = variables;
        this.requester = new Requester(args);
    }

    /**
     * Gets all of the headers.
     *
     * @return Headers.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> headers() {
        return (Map<String, String>) this.args.get("headers");
    }

    /**
     * Gets the value of a header.
     *
     * @param key Header key.
     * @return Header value or null if the header does not exist.
     */
    public String header(String key)
    {
        return this.headers().get(key);
    }

    /**
     * Gets the value of an header if it exists, or a default value.
     *
     * @param key Header key.
     * @param defaultValue Value returned if the header doesn't exist.
     * @return Header value, or default value.
     */
    public String header(String key, String defaultValue)
    {
        return (this.hasHeader(key)) ? this.header(key) : defaultValue;
    }

    /**
     * Checks if a header exists.
     *
     * @param key Header key.
     * @return True if the header exists, false otherwise.
     */
    public boolean hasHeader(String key)
    {
        return this.headers().containsKey(key);
    }

    /**
     * Puts a new message on the "message" argument.
     * 
     * @param message New message.
     */
    public void message(String message)
    {
        this.args.put("message", message);
        this.args.put("length", ((String) this.args.get("message")).length());
    }
    
    /**
     * Returns the request message.
     * 
     * @return Message received in the request.
     */
    public String message()
    {
        return (String) this.args.get("message");
    }
    
    /**
     * Returns the length of the original received message.
     * 
     * @return Message length.
     */
    public int length()
    {
        return (Integer) this.args.get("length");
    }

    /**
     * Returns the IPv4 and port of the requester, separated by a ':'.
     * 
     * @return IPv4 and port of the requester.
     */
    public Requester requester()
    {
        return this.requester;
    }
    
    /**
     * Gets the targeted route.
     * 
     * @return Route that the requester targeted.
     */
    public String route()
    {
        return (String) this.args.get("route");
    }

    /**
     * Returns the TCP socket. This method should be used for
     * TCP requests only.
     *
     * @return TCP Socket.
     */
    public Socket socket()
    {
        return (Socket) this.args.get("socket");
    }

    /**
     * Returns the input stream from the socket. This method
     * should be used for TCP requests only.
     *
     * @return Input Stream from the socket.
     */
    public BufferedReader input() throws IOException {
        InputStreamReader isr = new InputStreamReader(this.socket().getInputStream());

        return new BufferedReader(isr);
    }

    /**
     * Returns the output stream from the socket. This method should be used for
     * TCP requests only.
     *
     * @return Output Stream from the socket.
     */
    public PrintWriter output() throws IOException {
        return new PrintWriter(this.socket().getOutputStream(), true);
    }
    
    /**
     * Puts a list of values into a variable.
     *
     * @param variable Variable name.
     * @param values Variable values.
     */
    public void put(String variable, List<String> values) {
        this.variables.put(variable, values);
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
        return this.variables.get(variable);
    }

    public String listen() {
        byte[] response = new byte[1024];

        try {
            this.socket().getInputStream().read(response);
        } catch (IOException e) {
            throw new IllegalArgumentException("IO Exception.");
        }

        return new String(response).trim();
    }

    public <V> void reply(V value) throws IOException {
        if (this.output() != null) {
            this.output().println(value);
        }
    }

    /**
     * Checks if the requester is the same as the current user.
     * This is useful to block broadcast signals.
     * 
     * @return True if the requester is the same as the current user, false otherwise.
     */
    public boolean same()
    {
        String requester = this.requester().from();
        
        try {
            Enumeration<?> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration<?> ee = n.getInetAddresses();
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
