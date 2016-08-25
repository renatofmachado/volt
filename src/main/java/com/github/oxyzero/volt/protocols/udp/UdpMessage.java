package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.SocketMessage;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public class UdpMessage extends SocketMessage {

    private final String packet;

    private final String HEADER_DELIMITER = ":";

    private final String MESSAGE_SEPARATOR = "@";

    public UdpMessage(DatagramPacket request)
    {
        super();
        this.packet = new String(request.getData(), StandardCharsets.UTF_8);
        defineMessage();
        defineHeaders();
    }

    @Override
    public void defineMessage() {
        super.message = packet.split(MESSAGE_SEPARATOR)[1].trim();
    }

    @Override
    public void defineHeaders() {
        String[] tokens = packet.split(MESSAGE_SEPARATOR)[0].split(HEADER_DELIMITER);

        super.headers.put("packet-id",           tokens[0]);
        super.headers.put("packets",             tokens[1]);
        super.headers.put("route-checksum",      tokens[2]);
        super.headers.put("message-checksum",    tokens[3]);
    }
}
