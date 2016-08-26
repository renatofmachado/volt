package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.SocketMessage;
import com.github.oxyzero.volt.support.Checksum;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UdpMessage extends SocketMessage<DatagramPacket> {

    public UdpMessage() {
        super();
    }

    public UdpMessage(Map<String, String> headers,String message) {
        super();
        super.headers = headers;
        super.message = message;

    }

    @Override
    public String getHeaderDelimiter() {
        return "|";
    }

    @Override
    public String getHeaderDelimiterRegex() {
        return "(?<!\\\\)(\\|)";
    }

    @Override
    public String getMessageDelimiter() {
        return "|";
    }

    @Override
    public String getMessageDelimiterRegex() {
        return "(?<!\\\\)(\\|)";
    }

    @Override
    public String getSeparator() {
        return "@";
    }

    @Override
    public String getSeparatorRegex() {
        return "(?<!\\\\)(\\@)";
    }

    @Override
    public void toReceive(DatagramPacket receiver) {
        String[] packet = new String(receiver.getData(), StandardCharsets.UTF_8).split(this.getSeparatorRegex());

        String[] tokens = packet[0].split(this.getHeaderDelimiterRegex());

        super.headers.put("packet-id",           tokens[0]);
        super.headers.put("packets",             tokens[1]);
        super.headers.put("route-checksum",      tokens[2]);
        super.headers.put("message-checksum",    tokens[3]);

        for (int i = 4; i < tokens.length; i++) {
            String[] header = tokens[i].split(":");
            super.headers.put(header[0].trim(), header[1].trim());
        }

        super.message = packet[1].trim();
    }

    @Override
    public List<String> toSend(Request request) {
        String checksumRoute = Checksum.make(request.route());
        String checksumMessage = Checksum.make(request.message());

        int parts = this.calculateNumberOfPacketsToSend(request.length(), checksumRoute.length(), checksumMessage.length());

        List<String> messages = new ArrayList<>();
        String message = request.message();

        String packetHeaders = parts
                             + this.getHeaderDelimiter() + checksumRoute
                             + this.getHeaderDelimiter() + checksumMessage;

        for (Map.Entry<String, String> header : request.headers().entrySet()) {
            packetHeaders += this.getHeaderDelimiter() + header.getKey() + ":" + header.getValue();
        }

        packetHeaders += this.getSeparator();

        int length = packetHeaders.length();

        for (int i = 1; i <= parts; i++) {
            String packetId = i + this.getHeaderDelimiter();

            int headersLength = packetId.length() + length;

            int cut = 512 - headersLength;
            int messageLength = message.length();

            if (cut > messageLength) {
                cut = messageLength;
            }

            String part = message.substring(0, cut);
            message = message.substring(cut, message.length());

            messages.add(packetId + packetHeaders + part);
        }

        return messages;
    }

    private int calculateNumberOfPacketsToSend(int message, int checksumRoute, int checksumMessage) {
        int parts = 0, length = message;

        while (length > 0) {
            length -= 512;
            parts++;
        }

        // Counts the number of digits a packet can have, i.e "22:22" + the separator token ":".
        int partsOffset = String.valueOf(parts).length() * 2 + 1;

        // Counts the number of digits of each checksum and adds the separator token ":".
        int checksumOffset = checksumRoute + checksumMessage + 1;

        // Adds both offsets and the separator token "@".
        int offset = 512 - (partsOffset + checksumOffset + 1);

        length = message;
        parts = 0;

        while (length > 0) {
            length -= offset;
            parts++;
        }

        return parts;
    }
}
