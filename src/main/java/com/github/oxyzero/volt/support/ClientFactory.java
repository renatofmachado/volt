package com.github.oxyzero.volt.support;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.protocols.tcp.TcpClient;
import com.github.oxyzero.volt.protocols.udp.UdpClient;

public class ClientFactory {

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
