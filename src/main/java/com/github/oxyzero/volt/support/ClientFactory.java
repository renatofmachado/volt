package com.github.oxyzero.volt.support;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.protocols.tcp.TcpClient;
import com.github.oxyzero.volt.protocols.udp.UdpClient;

public class ClientFactory {

    /**
     * Makes a client using a dynamic port.
     *
     * @param protocol Client protocol.
     * @return Client instance.
     */
    public Client make(String protocol) {
        return this.make(protocol, 0);
    }

    /**
     * Makes a client using a given port.
     *
     * @param protocol Client protocol.
     * @param port Client port.
     * @return Client instance.
     */
    public Client make(String protocol, int port)
    {
        if (protocol.equalsIgnoreCase("udp")) {
            return new UdpClient(port);
        }

        if (protocol.equalsIgnoreCase("tcp")) {
            return new TcpClient(port);
        }

        throw new IllegalArgumentException("The protocol " + protocol + " is not a valid protocol.");
    }
}
