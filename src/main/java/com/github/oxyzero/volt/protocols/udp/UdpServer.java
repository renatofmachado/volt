package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.support.Task;
import com.github.oxyzero.volt.support.TaskManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class UdpServer extends Server {

    /**
     * Server instance.
     */
    private DatagramSocket server;
    
    /**
     * Routes.
     */
    private final Map<String, Connection> routes;
    
    /**
     * Given an hashed route, return the associated route.
     */
    private final Map<String, String> hashedRoutes;
    
    /**
     * Identifies the multiple packets sent by a requester.
     * 
     * Requester IPv4:Port
     *      - Message Checksum
     *          Packet ID - Message Part
     */
    protected final Map<String, Map<String, Map<Integer, String>>> packets;
    
    public UdpServer() {
        super();
        
        this.server = null;
        this.routes = new HashMap<>();
        this.packets = new HashMap<>();
        this.hashedRoutes = new HashMap<>();
    }

    public void close() {
        super.active = false;
    }

    public DatagramSocket server() {
        synchronized (this.server) {
            return this.server;
        }
    }

    @Override
    protected void boot(int port) {
        try {
            if (this.server != null) {
                this.restart(port);
                return;
            }
            
            super.active = true;
            super.connectedPort = port;
            this.server = new DatagramSocket(port);
            this.server.setSoTimeout(1000);
        } catch (SocketException ex) {
            super.connectedPort = -1;
            super.active = false;
            throw new IllegalArgumentException("Could not initiate the UDP service because the given port was already in use. " + ex.getMessage());
        }

    }
    
    @Override
    protected void restart(int port)
    {
        try {
            synchronized (this.server) {
                this.active = false;
                this.connectedPort = -1;
                this.server.disconnect();
                this.server.close();
                this.server = null;
                
                this.boot(port);
            }
        } catch (NullPointerException e) {
            this.boot(port);
        }
    }

    /**
     * Handles the given request route.
     *
     * @param request Request route.
     * @return True if successful in handling the route, false if they don't
     * match.
     */
    private synchronized Connection getActionFromRequest(String request, String message, Map<String, Object> arguments) {
        Connection action = null;
        String route = null;
        
        if (this.hashedRoutes.containsKey(request)) {
            route = this.hashedRoutes.get(request);
            action = this.routes.get(route);
        }
            
        if (action == null || route == null) {
            return null;
        }
        
        arguments.put("volt-message", message);
        arguments.put("volt-route", route);
        
        // We have found an action to perform, but now we need the route arguments.
        // Since we can pass up as much information as possible in a Packet,
        // we'll control the messages using a cyclic route. This means that we
        // need to break the message for each route cycle. For example:
        // Route: :name|:file
        // Message: Students|Students.json|foobar|foobar.xml
        // Gives:
        // Variable :name -> [ Students, foobar ]
        // Variable :file -> [ Students.json, foobar.xml ]
        
        String[] routeTokens = route.split("\\|");
        String[] requestTokens = ((String) arguments.get("volt-message")).split("\\|");
        
        int length = requestTokens.length / routeTokens.length;
        
        for (int i = 0; i < routeTokens.length; i++) {
            routeTokens[i] = routeTokens[i].substring(1);
            arguments.put(routeTokens[i], new ArrayList<>());
        }
        
        int item = 0;
        
        for (int i = 0; i < length; i++) {
            for (String routeToken : routeTokens) {
                ((List) arguments.get(routeToken)).add(requestTokens[item++]);
            }
        }

        return action;
    }
    
    /**
     * Gets the UDP packet headers.
     * 
     * @param packet Packet message.
     * @return Map containing the header names as key and header values as value.
     */
    private Map<String, String> getPacketHeaders(String packet) {
        Map<String, String> headers = new HashMap<>();

        String[] tokens = packet.split("@")[0].split(":");

        headers.put("id", tokens[0]);
        headers.put("count", tokens[1]);
        headers.put("route", tokens[2]);
        headers.put("checksum", tokens[3]);

        return headers;
    }
    
    /**
     * Streams the server in the given port.
     * 
     * @param port Service port.
     */
    @Override
    public void stream(int port) {
        if (this.isActive()) {
            return;
        }
        
        try {
            this.boot(port);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        
        while (isActive()) {
            try {
                
                final DatagramPacket request = new DatagramPacket(new byte[512], 512);

                this.server().receive(request);
                
                new Thread() {
                    @Override
                    public void run() {
                        final Map<String, String> headers;
                        final String packet;
                        final String client;
                        final String message;
                        final InetAddress address;
                        
                        synchronized (request) {
                            try {    
                                packet = new String(request.getData(), "UTF-8");
                            } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
                                this.interrupt();
                                return;
                            }

                            headers = getPacketHeaders(packet);
                            address = request.getAddress();
                            message = packet.split("@")[1].trim();

                            client = (request.getAddress() + ":" + request.getPort()).substring(1);
                        }
                        
                        if (! headers.get("count").equals("1")) {
                            synchronized (packets) {
                                if (! packets.containsKey(client)) {
                                    Map<String, Map<Integer, String>> map = new HashMap<>();
                                    packets.put(client, map);
                                }
                                   
                                if (! packets.get(client).containsKey(headers.get("checksum"))) {
                                    Map<Integer, String> id = new HashMap<>();
                                    id.put(Integer.parseInt(headers.get("id")), message);

                                    packets.get(client).put(headers.get("checksum"), id);

                                    TaskManager manager = new TaskManager();
                                    
                                    // Only happens if the checksum wasn't available in the first time.
                                    manager.after(5).once(new Task() {
                                        @Override
                                        public void fire() {
                                            
                                            int count = Integer.parseInt(headers.get("count"));

                                            if (packets.get(client).get(headers.get("checksum")).size() == count) {
                                                this.kill();
                                                manager.destroy();
                                                interrupt();
                                                return;
                                            }
                                            
                                            packets.get(client).remove(headers.get("checksum"));
                                            this.kill();
                                            manager.destroy();
                                            interrupt();
                                        }
                                    });
                                }
                                
                                packets.get(client).get(headers.get("checksum")).put(Integer.parseInt(headers.get("id")), message);
                                
                                int count = Integer.parseInt(headers.get("count"));
                                
                                if (packets.get(client).get(headers.get("checksum")).size() == count) {
                                    final StringBuilder builder = new StringBuilder();
                                    
                                    for (int i = 1; i <= count; i++) {
                                        builder.append(packets.get(client).get(headers.get("checksum")).get(i));
                                    }
                                    
                                    String requestMessage = builder.toString();
                                    
                                    handleAction(headers, client, address, requestMessage);
                                }
                            }
                        } else {
                            handleAction(headers, client, address, message);
                        }
                        
                    }
                }.start();
            } catch (IOException ex) {
                // Don't do anything.
            }
        }
        
        try {
            synchronized (this.server) {
                this.shutdown();
            }
        } catch (NullPointerException e) {}
    }
    
    private void handleAction(Map<String, String> headers, String client, InetAddress address, String message)
    {
        final Map<String, Object> arguments = new HashMap<>();
        
        Connection action = getActionFromRequest(headers.get("route"), message, arguments);

        if (action == null) {
            return;
        }

        arguments.put("volt-from", client);
        arguments.put("volt-packets", headers.get("count"));
        arguments.put("volt-hostname", address.getHostName());
        arguments.put("volt-address", address);
        arguments.put("volt-length", message.length());
        
        Request request = new Request(arguments);
        
        this.executeBeforeMiddlewares(request);

        synchronized (action) {
            action.run(request);
        }

        this.executeAfterMiddlewares(request);
    }

    /**
     * Sends a given message to the route of the target.
     *
     * @param route Route defined by the target.
     * @param target IPv4 and Port (separated by ":")
     * @param message Message to send, no headers.
     */
    @Override
    public void send(String route, String target, String message) {
        try {
            if (server() == null) {
                return;
            }
        } catch (NullPointerException e) {
            return;
        }

        Map<String, Object> arguments = new HashMap<>();
        arguments.put("volt-message", message);
        arguments.put("volt-length", message.length());
        arguments.put("volt-route", route);
        arguments.put("volt-target", target);
        arguments.put("volt-from", target);

        Request request = new Request(arguments);

        this.executeBeforeMiddlewares(request);

        message = request.message();

        int parts = 0, length = message.length();

        CRC32 checkRoute = new CRC32();
        CRC32 checkMessage = new CRC32();

        try {
            checkRoute.update(route.getBytes("UTF-8"));
            checkMessage.update(route.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Check if the message is a multipart message.
        if (length > 512) {
            while (length > 0) {
                length -= 512;
                parts++;
            }

            // Simulate if we need more parts to complete the message.
            int overhead = 0;
            for (int i = 1; i <= parts; i++) {
                overhead += (i + ":" + parts + ":" + checkRoute.getValue() + ":" + checkMessage.getValue() + "@").length();
            }

            length += overhead;

            while (length > 512) {
                length -= 512;
                parts++;
            }
        }

        this.protocol(route, target, parts, message);
    }

    /**
     * Sets a action for a expected route.
     *
     * @param route Expected route.
     * @param action Action to be executed.
     */
    @Override
    public void listen(String route, Connection action) {
        CRC32 checksum = new CRC32();

        try {
            checksum.update(route.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        synchronized (this.routes) {
            this.routes.put(route, action);
            this.hashedRoutes.put(String.valueOf(checksum.getValue()), route);
        }
    }

    @Override
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
    public void forget(String route) {
        synchronized (this.routes) {
            this.routes.remove(route);

            for (Map.Entry<String, String> entry : this.hashedRoutes.entrySet()) {
                if (entry.getValue().equals(route)) {
                    this.hashedRoutes.remove(entry.getKey());
                    break;
                }
            }
        }

        synchronized (this.middlewares) {
            this.middlewares.remove(route);
        }
    }

    /**
     * Gets all of the routes.
     *
     * @return Routes.
     */
    public Map<String, Connection> routes() {
        synchronized (this.routes) {
            return this.routes;
        }
    }

    /**
     * Protocol to be used by the server.
     *
     * @param route The target route.
     * @param target The target the server will contact.
     * @param parts The number of parts the server should send.
     * @param message The message to be sent.
     */
    protected void protocol(String route, String target, int parts, String message)
    {
        if (target.startsWith("255.255.255.255")) {
            try {
                this.server().setBroadcast(true);
            } catch (SocketException ex) {
                Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (target.startsWith("all")) {
            target = target.replace("all", "255.255.255.255");
            try {
                this.server().setBroadcast(true);
            } catch (SocketException ex) {
                Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        CRC32 checksumRoute = new CRC32();
        CRC32 checksumMessage = new CRC32();

        try {
            checksumMessage.update(message.getBytes("UTF-8"));
            checksumRoute.update(route.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(UdpServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (parts > 0) {
            String[] messages = new String[parts];

            for (int i = 1; i <= parts; i++) {
                String header = i + ":" + parts + ":" + checksumRoute.getValue()
                        + ":" + checksumMessage.getValue() + "@";
                int length = header.length();
                int cut = 512 - length;
                int messageLength = message.length();

                if (cut > messageLength) {
                    cut = messageLength;
                }

                String part = message.substring(0, cut);
                message = message.substring(cut, message.length());

                messages[i - 1] = header + part;

            }

            String[] targetData = target.split(":");

            if (targetData.length != 2) {
                throw new IllegalArgumentException("The target must be consisted of a IPv4 and a Port separated by \":\". Example: all:8000");
            }

            int port = Integer.parseInt(targetData[1]);

            InetAddress address;

            try {
                address = InetAddress.getByName(targetData[0]);
            } catch (UnknownHostException ex) {
                return;
            }

            for (String part : messages) {
                try {
                    DatagramPacket packet = new DatagramPacket(part.getBytes("UTF-8"), part.length(), address, port);

                    this.server().send(packet);

                } catch (UnknownHostException | UnsupportedEncodingException | SocketException ex) {
                    return;
                } catch (IOException ex) {
                    return;
                }
            }

            Map<String, Object> arguments = new HashMap<>();
            arguments.put("volt-message", message);
            arguments.put("volt-length", message.length());
            arguments.put("volt-route", route);
            arguments.put("volt-target", target);
            arguments.put("volt-from", target);
            arguments.put("volt-hostname", address.getHostName());
            arguments.put("volt-address", address);
            arguments.put("volt-packets", parts);

            Request request = new Request(arguments);

            this.executeAfterMiddlewares(request);

            try {
                this.server().setBroadcast(false);
                return;
            } catch (SocketException ex) {
                return;
            }
        } else {
            String header = "1:1:" + checksumRoute.getValue()
                    + ":" + checksumMessage.getValue();

            String packetMessage = header + "@" + message;

            String[] targetData = target.split(":");

            if (targetData.length != 2) {
                throw new IllegalArgumentException("The target must be consisted of a IPv4 and a Port separated by \":\". Example: all:8000");
            }

            int port = Integer.parseInt(targetData[1]);

            try {
                InetAddress address = null;

                try {
                    address = InetAddress.getByName(targetData[0]);
                } catch (UnknownHostException ex) {
                    return;
                }

                DatagramPacket packet = new DatagramPacket(packetMessage.getBytes("UTF-8"), packetMessage.length(), address, port);
                this.server().send(packet);
                this.server().setBroadcast(false);

                Map<String, Object> arguments = new HashMap<>();
                arguments.put("volt-message", message);
                arguments.put("volt-length", message.length());
                arguments.put("volt-route", route);
                arguments.put("volt-target", target);
                arguments.put("volt-from", target);
                arguments.put("volt-hostname", address.getHostName());
                arguments.put("volt-address", address);
                arguments.put("volt-packets", parts);

                Request request = new Request(arguments);

                this.executeAfterMiddlewares(request);
            } catch (UnknownHostException | UnsupportedEncodingException | SocketException ex) {
                return;
            } catch (IOException ex) {
                return;
            }
        }
    }
    
    /**
     * Shuts the server down.
     */
    @Override
    public void shutdown()
    {
        try {
            synchronized (this.server) {
                try {
                    super.active = false;
                    
                    if (this.server == null) {
                        return;
                    }
                    
                    this.server.disconnect();
                    this.server.close();

                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
}
