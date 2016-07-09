package com.github.oxyzero.volt;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Connection {

    /**
     * Connection server.
     */
    private Server server;
    
    /**
     * Connection socket.
     */
    private Socket socket;
    
    /**
     * Connection input stream.
     */
    private BufferedReader input;
    
    /**
     * Connection output stream.
     */
    private PrintWriter output;
    
    /**
     * Connection requester.
     */
    private Requester requester;
    
    /**
     * Handles a request.
     * 
     * @param request Received request.
     **/
    public abstract void run(Request request);
    
    /**
     * Hook that gets triggered before the request gets handled.
     */
    public void onEnter() {}

    /**
     * Hook that triggers after the request was handled.
     */
    public void onExit() {}
    
    /**
     * Determines when a connection can be closed.
     * 
     * Usually this should be meant for direct connections.
     * 
     * @return True if it the connection can be disconnected, false otherwise.
     */
    public boolean canDisconnect() {
        return false;
    }
    
    /**
     * Gets the TCP Socket.
     * 
     * @return TCP Socket if the connection is a TCP connection.
     */
    public Socket socket() {
        return this.socket;
    }
    
    /**
     * Sets the TCP Socket for this connection.
     * 
     * @param socket TCP Socket.
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
    /**
     * Gets the TCP input stream.
     * 
     * @return TCP Input Stream if the connection is a TCP connection.
     */
    public BufferedReader input() {
        return this.input;
    }
    
    /**
     * Sets the TCP input stream.
     * 
     * @param input TCP input stream.
     */
    public void setInput(BufferedReader input) {
        this.input = input;
    }
    
    /**
     * Gets the TCP output stream.
     *
     * @return TCP Output Stream if the connection is a TCP connection.
     */
    public PrintWriter output() {
        return this.output;
    }
    
    /**
     * Sets the TCP output stream.
     *
     * @param output TCP output stream.
     */
    public void setOutput(PrintWriter output) {
        this.output = output;
    }
    
    /**
     * Gets the requester of the connection.
     * 
     * @return Requester of the connection.
     */
    public Requester requester() {
        return this.requester;
    }
    
    /**
     * Sets the requester of the connection.
     *
     * @param requester Requester of the connection.
     */
    public void setRequester(Requester requester) {
        this.requester = requester;
    }
    
    /**
     * Gets the server of the connection.
     * 
     * @return Server.
     */
    public Server server() {
        return this.server;
    }
    
    /**
     * Sets the server of the connection.
     * 
     * @param server Server.
     */
    public void setServer(Server server) {
        this.server = server;
    }
}
