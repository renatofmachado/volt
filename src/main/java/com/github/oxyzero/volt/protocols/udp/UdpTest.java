package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.Server;
import com.github.oxyzero.volt.Volt;

import java.util.Scanner;

public class UdpTest {

    public static void main(String[] args) {
        Server server = Volt.server("tcp", 30600);

        server.listen(":calculator", (request) -> {
            String operation = null;

            while (true) {
                operation = request.listen();

                if (operation == null) {
                    continue;
                }

                if (operation.equals("exit")) {
                    break;
                }

                String[] data = operation.split("\\+");

                int result = 0;
                for (String number : data) {
                    number = number.trim();

                    if (number.isEmpty()) {
                        continue;
                    }

                    result += Integer.parseInt(number.trim());
                }

                request.reply(result);
                System.out.print("");

            }
        });

        Client client = Volt.client("tcp");

        Scanner scanner = new Scanner(System.in);

        client.send(":calculator", Volt.localhost(30600), (request) -> {
            while (true) {
                String argument = "";
                    System.out.println("Sum: ");
                    argument = scanner.nextLine();

                    request.reply(argument.trim());

                    if (argument.equals("exit")) {
                        return;
                    }

                    String result = request.listen();

                    System.out.println("Result: " + result);
                    System.out.print("");
            }
        });

        client.after(1).every(1).send(":message", "all:30600", "Hello Volt!")
              .after(5).stop();
    }

}
