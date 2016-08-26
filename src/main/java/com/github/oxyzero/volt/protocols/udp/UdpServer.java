package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.Socket;
import com.github.oxyzero.volt.support.RequestBuilder;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class UdpServer extends Server {

    private Socket<DatagramSocket> socket;

    /**
     * Identifies the multiple packets sent by a requester.
     *
     * Target-Route Checksum-Message Checksum
     *      Packet ID => Message Part
     */
    private final Map<String, Map<Integer, String>> packets;

    public UdpServer()
    {
        super();
        this.socket = new UdpSocket();
        this.packets = new HashMap<>();
    }

    @Override
    protected Socket<DatagramSocket> socket() {
        return this.socket;
    }

    public Map<String, Map<Integer, String>> packets() {
        return this.packets;
    }

    @Override
    public void stream(int port) {
        if (this.isActive()) {
            return;
        }

        this.boot(port);

        while (this.isActive()) {
            try {
                final DatagramPacket request = (DatagramPacket) this.socket().receive();

                new UdpStream(this, request).start();

            } catch (Exception e) {
                // Ignore the exception due to the server timeout.
            }
        }

        this.shutdown();
    }

    @Override
    public void listen(String route, Connection action) {
        this.router().register(route, new UdpRoute(route, action));
    }

    @Override
    public void send(String route, String target, String message, String... headers) {
        try {
            if (this.socket().server() == null) {
                this.boot(0);
            }
        } catch (NullPointerException e) {
            return;
        }

        // TODO: Handle Target Aliases

        target = target.replace("all", "255.255.255.255");

        if (target.startsWith("255.255.255.255")) {
            try {
                this.socket.server().setBroadcast(true);
            } catch (SocketException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.message(message);
        requestBuilder.route(route);
        requestBuilder.target(target);
        requestBuilder.headers(headers);

        Request request = requestBuilder.build();

        this.socket().send(request);

        this.socket().shutdown();
    }

}
