package com.github.oxyzero.volt.protocols.tcp;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a client that allows to quickly send a message to
 * a Volt server instance.
 * 
 * @author Renato Machado
 */
public class TcpClient extends Client {
   
    /**
     * Creates a new TCP Client using a dynamic port.
     */
    public TcpClient() {
        super(new TcpServer(), 0);
    }
    
    /**
     * Creates a new TCP Client.
     * 
     * @param port Client port. Use 0 to use a dynamic port.
     */
    public TcpClient(int port) {
        super(new TcpServer(), port);
    }
    
    /**
     * Sends a given set of headers with a message to the given target.
     *
     * @param headers Set of headers separated by ";". This headers follow a
     * strict structure:
     *
     * route;encrypted
     *
     * Route: (String) Gives the target route to where this message will land.
     * Encrypted: (Boolean) True if the embedded message is currently encrypted.
     *
     * @param target Target defined by IPv4:Port.
     * @param message Message.
     */
    @Override
    public Client send(String headers, String target, String message) {
        // Redeclared method just to override the documentation.
        return super.send(headers, target, message);
    }
    
    public void direct(String headers, String target, Connection connection) {
        String[] host = target.split(":");
        int port = Integer.valueOf(host[1]);
        
        try {
            Socket socket = new Socket(host[0], port);
            
            String message = "Direct-connection: open";
            String data[] = headers.split(";");
            StringBuilder builder = new StringBuilder();

            builder.append(data[0]);
            builder.append(" ");
            builder.append(message.length());
            builder.append("\r\n");

            for (int i = 1; i < data.length; i++) {
                builder.append(data[i]);
                builder.append("\r\n");
            }

            builder.append("\r\n");
            builder.append(message);
            
            String finalProduct = builder.toString();
            
            ((TcpServer) this.client()).reply(socket, finalProduct);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            
            connection.setServer((TcpServer) this.client());
            connection.setSocket(socket);
            connection.setInput(input);
            connection.setOutput(output);
            
            connection.onEnter();
            
            do {
                connection.run(null);
            } while (! connection.canDisconnect());
            
            connection.onExit();
            
            socket.getInputStream().close();
            socket.getOutputStream().close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TcpClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
