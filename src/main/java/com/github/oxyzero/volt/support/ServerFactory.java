package com.github.oxyzero.volt.support;

import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.protocols.tcp.TcpServer;
import com.github.oxyzero.volt.protocols.udp.UdpServer;

public class ServerFactory {

    /**
     * Creates a server with the given protocol.
     *
     * @param protocol Server protocol.
     * @return Server inactive instance.
     */
    public Server create(String protocol) {
        if (protocol.equalsIgnoreCase("udp")) {
            return new UdpServer();
        } else if (protocol.equalsIgnoreCase("tcp")) {
            return new TcpServer();
        }

        throw new IllegalArgumentException("The protocol " + protocol + " is invalid.");
    }

    /**
     * Creates and streams a server with the given protocol.
     *
     * @param protocol Server protocol.
     * @param port Server port.
     * @return Server active instance.
     */
    public Server make(String protocol, int port) {
        final Server server = this.create(protocol);

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
