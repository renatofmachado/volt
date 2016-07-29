package com.github.oxyzero.volt.support;

import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.protocols.tcp.TcpServer;
import com.github.oxyzero.volt.protocols.udp.UdpServer;

public class ServerFactory {

    public Server make(String protocol, int port) {
        final Server server;

        if (protocol.equalsIgnoreCase("udp")) {
            server = new UdpServer();
        } else if (protocol.equalsIgnoreCase("tcp")) {
            server = new TcpServer();
        } else {
            throw new IllegalArgumentException("The protocol " + protocol + " is invalid.");
        }

        try {
            new Thread() {
                @Override
                public void run() {
                    server.stream(port);
                }
            }.start();
        } catch (IllegalArgumentException e) {
            throw e;
        }

        return server;
    }
}
