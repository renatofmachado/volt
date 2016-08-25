public class VoltTest {

    public static void main(String[] args)
    {
//        Server server = Volt.server("udp", 30600);
//
//        server.listen(":calculator", (request) -> {
//            while (true) {
//                String operation = request.listen();
//
//                if (operation.equalsIgnoreCase("exit")) {
//                    break;
//                }
//
//                String[] numbers = operation.split("\\+");
//                int result = 0;
//
//                for (String number : numbers) {
//                    result += Integer.valueOf(number.trim());
//                }
//
//                request.reply(result);
//            }
//        });
//
//        Client client = Volt.client("udp");
//
//        client.send(":calculator", Volt.localhost(30600), (request) -> {
//            Scanner scanner = new Scanner(System.in);
//
//            while (true) {
//                System.out.print("Sum: ");
//                String input = scanner.nextLine();
//
//                request.reply(input);
//
//                if (input.equalsIgnoreCase("exit")) {
//                    break;
//                }
//
//                System.out.println("Result: " + request.listen());
//            }
//        });
    }
}
