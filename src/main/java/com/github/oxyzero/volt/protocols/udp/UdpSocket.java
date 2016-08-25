package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.Socket;
import com.github.oxyzero.volt.support.Checksum;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class UdpSocket implements Socket<DatagramSocket> {

    private DatagramSocket socket;

    @Override
    public synchronized DatagramSocket boot(Object... args) {
        if (this.socket != null) {
            return this.restart(args);
        }

        if (args.length < 1) {
            throw new IllegalArgumentException("Not enough arguments were provided to boot the socket.");
        }

        try {
            this.socket = new DatagramSocket((Integer) args[0]);
            this.socket.setSoTimeout(1000);

            return this.socket;
        } catch (SocketException e) {
            throw new IllegalArgumentException("Not able to boot the socket because the port " + args[0] + " is already being used.");
        }
    }

    @Override
    public synchronized DatagramSocket restart(Object... args) {
        if (this.socket == null) {
            return this.boot(args);
        }

        this.shutdown();

        return this.boot(args);
    }

    @Override
    public synchronized void shutdown() {
        if (this.socket == null) {
            return;
        }

        this.socket.disconnect();
        this.socket.close();
        this.socket = null;
    }

    @Override
    public synchronized DatagramSocket server() {
        return this.socket;
    }

    @Override
    public synchronized int getPort() {
        return this.socket.getPort();
    }

    @Override
    public synchronized int getLocalPort() {
        return this.socket.getLocalPort();
    }

    @Override
    public synchronized Object receive() throws Exception {
        final DatagramPacket request = new DatagramPacket(new byte[512], 512);

        this.socket.receive(request);

        return request;
    }

    @Override
    public void send(Request request) {
        String checksumRoute = Checksum.make(request.route());
        String checksumMessage = Checksum.make(request.message());

        int parts = this.calculateNumberOfPacketsToSend(request.length(), checksumRoute.length(), checksumMessage.length());

        String[] messages = new String[parts];
        String message = request.message();

        for (int i = 1; i <= parts; i++) {
            String headers = i + ":" + parts + ":" + checksumRoute + ":" + checksumMessage + "@";

            int cut = 512 - headers.length();
            int messageLength = message.length();

            if (cut > messageLength) {
                cut = messageLength;
            }

            String part = message.substring(0, cut);
            message = message.substring(cut, message.length());

            messages[i - 1] = headers + part;
        }

        InetAddress address;

        try {
            address = InetAddress.getByName(request.requester().from());
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }

        for (String part : messages) {
            try {
                DatagramPacket packet = new DatagramPacket(part.getBytes(StandardCharsets.UTF_8), part.length(), address, request.requester().port());

                this.server().send(packet);

            } catch (IOException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        // TODO: Execute After Middleware
        // this.executeAfterMiddlewares(request);

        try {
            this.server().setBroadcast(false);
        } catch (SocketException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
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
