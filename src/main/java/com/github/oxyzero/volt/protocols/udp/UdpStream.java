package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.RequestBuilder;
import com.github.oxyzero.volt.support.Task;
import com.github.oxyzero.volt.support.TaskManager;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class UdpStream extends Thread {

    private DatagramPacket packet;

    private UdpServer server;

    public UdpStream(UdpServer server, DatagramPacket packet) {
        this.server = server;
        this.packet = packet;
    }

    private void handleAction(String route, Request request)
    {
        // @TODO: Implement Middleware support.
        // this.executeBeforeMiddlewares(request);

        this.server.router().handle(route, request);

        // @TODO: Implement Middleware support.
        // this.executeAfterMiddlewares(request);
    }

    @Override
    public void run() {
        RequestBuilder requestBuilder = new RequestBuilder();

        UdpMessage message;
        String target;

        synchronized (this.packet) {
            message = new UdpMessage(this.packet);
            target = (this.packet.getAddress() + ":" + this.packet.getPort()).substring(1);
            requestBuilder.target(target);
        }

        final String route = this.server.router().resolve(message.header("route-checksum"));
        requestBuilder.route(route);

        Integer numberOfPackets = Integer.parseInt(message.header("packets"));

        if (numberOfPackets == 1) {
            requestBuilder.message(message.message());
            handleAction(route, requestBuilder.build());
            return;
        }

        String client = target + "-" + message.header("route-checksum") + "-" + message.header("message-checksum");

        Map<String, Map<Integer, String>> packets = this.server.packets();

        synchronized (packets) {
            // The packet we received is the first of many.
            if (!packets.containsKey(client)) {
                Map<Integer, String> parts = new HashMap<>();
                packets.put(client, parts);
            }

            Integer packetId = Integer.parseInt(message.header("packet-id"));
            packets.get(client).put(packetId, message.message());

            if (packets.get(client).size() != numberOfPackets) {
                TaskManager manager = new TaskManager();

                // Only happens if the checksum wasn't available in the first time.
                manager.after(5).once(new Task() {
                    @Override
                    public void fire() {
                        if (packets.get(client) != null && packets.get(client).size() == numberOfPackets) {
                            this.kill();
                            manager.destroy();
                            interrupt();
                            return;
                        }

                        packets.remove(client);
                        this.kill();
                        manager.destroy();
                        interrupt();
                    }
                });

                return;
            }

            // We've received all of the packets from this message.
            final StringBuilder builder = new StringBuilder();

            for (int i = 1; i <= numberOfPackets; i++) {
                builder.append(packets.get(client).get(i));
            }

            requestBuilder.message(builder.toString());

            handleAction(route, requestBuilder.build());

            packets.remove(client);
        }
    }

}
