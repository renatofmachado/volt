package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.*;

public class UdpTest {

    public static void main(String[] args) {
        Volt.kill(30600, 2);

        Server server = Volt.server("udp", 30600);

        server.listen(":package|:version", (request) -> {
            System.out.println(request.message());
            System.out.println("Package: " + request.get("package").get(0));
            System.out.println("Version: " + request.get("version").get(0));
            System.out.println("Server listened the request on port: " + server.getPort());
        });

        Client client = Volt.client("udp");

        client.every(1).send(":package|:version", Volt.localhost(30600), "Volt|v0.1")
              .after(2).stop();
    }


}
