package com.github.oxyzero.volt.protocols.tcp;

import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class represents a TCP server that is able to receive and respond to
 * requests.
 *
 * @author Renato Machado
 */
public class TcpServer extends Server {

    /**
     * Server socket.
     */
    protected ServerSocket server;

    /**
     * Routes.
     */
    protected final Map<String, Connection> routes;

    public TcpServer()
    {
        super();
        this.routes = new HashMap<>();
    }

    public ServerSocket server()
    {
        synchronized (this.server) {
            return this.server;
        }
    }

    /**
     * Boots the server.
     *
     * @param port The port number.
     */
    @Override
    protected void boot(int port)
    {
        try {
            if (this.server != null) {
                this.restart(port);
                return;
            }
            
            super.active = true;
            super.connectedPort = port;
            this.server = new ServerSocket(port);
            this.server.setSoTimeout(1000);
        } catch (IOException ex) {
            super.connectedPort = -1;
            super.active = false;
            throw new IllegalArgumentException("Could not initiate the TCP service because the given port was already in use. " + ex.getMessage());
        }
    }
    
    @Override
    protected void restart(int port) {
        try {
            synchronized (this.server) {
                this.active = false;
                this.connectedPort = -1;
                this.server.close();
                this.server = null;

                this.boot(port);
            }
        } catch (NullPointerException e) {
            this.boot(port);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /**
     * Closes all open connections from the server.
     */
    @Override
    public void shutdown()
    {
        try {
            synchronized (this.server) {
                try {
                    if (this.server != null) {
                        this.server.close();
                        this.server = null;
                    }

                    super.active = false;
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Streams the server in the given port.
     *
     * @param port The port number.
     */
    @Override
    public void stream(int port)
    {
        if (this.isActive()) {
            return;
        }

        this.boot(port);

        while (this.isActive()) {
            try {
                final Socket socket = this.server.accept();

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                            BufferedReader input = new BufferedReader(isr);
                            PrintWriter output = new PrintWriter(socket.
                                    getOutputStream(), true);

                            protocol(socket, input, output);
                            
                            isr.close();
                            output.close();
                            socket.close();
                        } catch (IOException ex) {
                            // Don't do anything.
                        }
                    }
                }.start();
            } catch (IOException ex) {
                // Don't do anything.
            }
        }

    }

    /**
     * Expects for a request to ask for this route.
     *
     * @param route Route.
     * @param action Action to execute if the request matches this route.
     */
    @Override
    public void listen(String route, Connection action)
    {
        synchronized (this.routes) {
            this.routes.put(route, action);
        }
    }

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
     * Neglects an expected route.
     *
     * @param route Route to be neglected.
     */
    @Override
    public void forget(String route)
    {
        synchronized (this.routes) {
            this.routes.remove(route);
        }
        
        synchronized (this.middlewares) {
            this.middlewares.remove(route);
        }
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
    public void send(String headers, String target, String message)
    {
        try {
            synchronized (this.server) {
                if (this.server.isClosed()) {
                    throw new IllegalArgumentException("This Tcp instance needs to have a open server to be able to communicate.");
                }
            }
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("A server needs to be booted in order to be able to communicate.");
        }
        
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
        
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("volt-route", data[0]);
        arguments.put("volt-message", message);
        
        Request request = new Request(arguments);
        
        this.executeBeforeMiddlewares(request);

        message = (String) arguments.get("volt-message");

        builder.append("\r\n");
        builder.append(message);

        this.protocol(data[0], target, builder.toString());
    }

    /**
     * Communication Protocol.
     *
     * @param socket Connected socket.
     * @param input Server input stream.
     * @param output Server output stream.
     */
    protected void protocol(Socket socket, BufferedReader input, PrintWriter output)
    {
        // Handle the request headers.
        String headers = this.getHeaders(input);

        if (headers == null || headers.isEmpty()) {
            return;
        }

        Map<String, Object> args = new HashMap<>();

	// Request example:
        // :route 250\r\n
        // Other headers\r\n
        // Message
        String[] request = headers.split("\n");

        String[] first = request[0].split(" ");
        Connection action = null;

        synchronized (this.routes) {
            if (!this.routes.containsKey(first[0])) {
                return;
            }

            action = this.routes.get(first[0]);
        }

        args.put("volt-route", first[0]);
        args.put("volt-length", first[1]);

        int i = 1;

        for (; i < request.length; i++) {
            if (request[i].isEmpty()) {
                break;
            }

            // Handle other headers.
            String[] line = request[i].split(":");
            args.put(line[0], line[1]);
        }

        StringBuilder message = new StringBuilder();

        i++;

        for (; i < request.length; i++) {
            message.append(request[i]);
        }

        args.put("volt-message", message.toString());
        args.put("volt-from", socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
        args.put("volt-hostname", socket.getInetAddress().getHostName());
        args.put("volt-input", input);
        args.put("volt-output", output);
        args.put("volt-socket", socket);
        args.put("volt-address", socket.getInetAddress());
        
        Request data = new Request(args);
        
        this.executeBeforeMiddlewares(data);
        
        action.run(new Request(args));
        
        this.executeAfterMiddlewares(data);
    }

    /**
     * Communication Protocol.
     *
     * @param route Target route.
     * @param target Target address and port (IPv4:Port)
     * @param message Message to be sent (includes headers)
     */
    protected void protocol(String route, String target, String message)
    {
        String[] targetData = target.split(":");

        if (targetData.length < 2) {
            throw new IllegalArgumentException("Target must be defined as IPv4:Port.");
        }

        try {
            Socket socket = new Socket(targetData[0], Integer.parseInt(targetData[1]));
            this.reply(socket, message);
            
            final Map<String, Object> args = new HashMap<>();
            
            args.put("volt-message", message);
            args.put("volt-route", route);
            args.put("volt-length", message.length());
            args.put("volt-from", socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            args.put("volt-hostname", socket.getInetAddress().getHostName());
            args.put("volt-input", new BufferedReader(new InputStreamReader(socket.getInputStream())));
            args.put("volt-output", new PrintWriter(socket.getOutputStream(), true));
            args.put("volt-socket", socket);
            args.put("volt-address", socket.getInetAddress());
            args.put("volt-target", target);
            
            Request data = new Request(args);
            
            this.executeAfterMiddlewares(data);
            
            socket.close();
        } catch (IOException ex) {
            // Don't do anything.
        }
    }

    /**
     * Gets the headers, which follow the HTTP format, from the input stream.
     *
     * @param input Input stream.
     * @return Headers.
     */
    private String getHeaders(BufferedReader input)
    {
        String headers = null;

        try {
            String lines = input.readLine();
            headers = lines;

            while (lines != null && !lines.isEmpty()) {
                lines = input.readLine();
                headers += '\n' + lines;
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        return headers;
//        String headers = null;
//
//        try {
//            String lines = input.readLine();
//            headers = lines;
//
//            while ((lines = input.readLine()) != null) {
//                headers += '\n' + lines;
//                
//                if (headers.endsWith("\r\n")) {
//                    break;
//                }
//            }
//        } catch (IOException ex) {
//            throw new IllegalArgumentException(ex.getMessage());
//        }
//
//        return headers;
    }

    /**
     * Allows the server to send a response to a socket.
     *
     * @param socket Socket.
     * @param response Response object.
     */
    protected void reply(Socket socket, Object response)
    {
        if (response instanceof String) {
            try {
                response = ((String) response).getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        try {
            socket.getOutputStream().write((byte[]) response);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

}
