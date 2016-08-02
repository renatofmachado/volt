package com.github.oxyzero.volt.protocols.tcp;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.Task;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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

    @Override
    public Client send(String headers, String target, Connection connection) {
        Task request = new Task() {
            @Override
            public void fire() {
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

                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                    Map<String, Object> args = new HashMap<>();
                    args.put("volt-message", "");
                    args.put("volt-length", 0);
                    args.put("volt-route", data[0]);
                    args.put("volt-target", target);

                    String address = target.split(":")[0];
                    args.put("volt-address", InetAddress.getByName(address));

                    args.put("volt-socket", socket);
                    args.put("volt-input", input);
                    args.put("volt-output", output);

                    Request request = new Request(args);

                    reply(request, finalProduct);

                    connection.setServer(client());
                    connection.setSocket(socket);
                    connection.setInput(input);
                    connection.setOutput(output);

                    connection.onEnter();
                    connection.run(request);
                    connection.onExit();

                    if (! socket.isClosed()) {
                        socket.getInputStream().close();
                        return;
                    }

                    if (! socket.isClosed()) {
                        socket.getOutputStream().close();
                        return;
                    }

                    if (! socket.isClosed()) {
                        socket.close();
                        return;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TcpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        super.fire(request);

        return this;
    }

    @Override
    public Client send(String headers, String target, Consumer<Request> action) {
        Connection connection = new Connection() {
            @Override
            public void run(Request request) {
                action.accept(request);
            }
        };

        return this.send(headers, target, connection);
    }

    @Override
    public Client reply(Request request, Object response) {
        if (response instanceof String) {
            try {
                response = ((String) response).getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        try {
            request.socket().getOutputStream().write((byte[]) response);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        return this;
    }
}
