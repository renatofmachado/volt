package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.Volt;

public class UdpTest {

    public static void main(String[] args) {
        Volt.kill(30600, 2);

        Server server = Volt.udp(30600);

        server.listen(":hello", request -> {
            System.out.println(request.message());
        });

        UdpClient client = new UdpClient();
        client.send(":hello", "all:30600", "Meet Volt.")
            .after(1).stop();

        /**Volt.debug();

        Server server = Volt.udp(30600);

        server.channel("*", new MessageDecryptionMiddleware("AcklWq203FgSSVgH"));

        server.listen(":hello", new Connection() {
            
            @Override
            public void run(Request request) {
                System.out.println(request.get("hello").get(0));
                
            }
        });
        
        server.listen(":broadcast", new Connection() {
            @Override
            public void run(Request request) {

                // Improved same method.
//                if (request.same()) {
//                    return;
//                }

                System.out.println("Message: " + request.message());
                System.out.println("From: " + request.from());
                System.out.println("Port: " + request.port());
                System.out.println("Requester: " + request.requester());
                System.out.println("Hostname: " + request.hostname());
                System.out.println("Route: " + request.route());
                System.out.println("Length: " + request.length());
                System.out.println("Packets: " + request.packets());
                System.out.println("\n\n");
                
                server.send(":hello", server.target(request.from()), "Hello there.");
            }
        });

        final UdpClient client = new UdpClient(0);
        
        client.client().channel("*", new MessageEncryptionMiddleware("AcklWq203FgSSVgH"));
        
        final TaskManager tm = new TaskManager();
        
        tm.after(2).every(3).fire(new Task() {
            public void fire() {
                System.out.println("Number of active threads from the given thread: " + Thread.activeCount());
                
                client.send(":broadcast", "all:30600", "Hello from Volt.");
                client.send(":broadcast", "all:30600", "Hello from Volt. Hello from Volt. Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt.Hello from Volt. YOOOOO");
            }
        })
                .after(20).once(new Task() {
                    public void fire() {
                        Volt.kill(30600);
                        tm.destroy();
                    }
                }
                );
    */
    }

}
