package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.Volt;

public class UdpTest {

    public static void main(String[] args) {
        Volt.kill(30600, 3);

        Server server = Volt.server("udp", 30600);

        server.listen(":package|:version", (request) -> {
            System.out.println("Key: " + request.header("key"));
            System.out.println(request.message());
            System.out.println("Package: " + request.get("package"));
            System.out.println("Version: " + request.get("version"));
            System.out.println("Server listened the request on port: " + server.getPort());
        });

        Client client = Volt.client("udp");

        client.every(1).send(":package|:version", Volt.localhost(30600), "Volt|v0.1\\|v0.2|Test|v1|Another|v2", "key:BCA21BE268")
                .after(2).stop();
    }


}
