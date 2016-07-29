package com.github.oxyzero.volt.protocols.tcp;

public class TcpServerTest {

    public static void main(String[] args) {
        // Prototype
        
        /**List<Requester> users = new ArrayList<>();
        
        TcpServer server = Volt.tcp(0);
        
        server.listen(":chat", new Connection() {
            
            @Override
            public void onEnter() {
                System.out.println("A new user joined in!");
            }
            
            @Override
            public void onExit() {
                System.out.println("A user has left.");
            }

            @Override
            public void run(Request request) {

            }
            /**
            @Override
            public boolean canDisconnect() {
                return this.members().isEmpty();
            }
            
            @Override
            public void run(Request request) {
                for (Requester user : users) {
                    if (! user.id().equals(this.member().id())) {
                        this.reply(user, request().message());
                    }
                }
            }
            /
            
        });
                
            **/
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
        
        
//        TcpServer server = Volt.tcp(20000);
//        
//        server.expect(":calculator", new Action() {
//            @Override
//            public void run(Request request) {
//                String operation = null;
//                BufferedReader input = request.input();
//                PrintWriter output = request.output();
//                Socket socket = request.socket();
//                
//                while (true) {
//                    try {
//                        byte[] response = new byte[1024];
//                        socket.getInputStream().read(response);
//                        
//                        operation = new String(response).trim();
//                        
//                        if (operation == null) {
//                            continue;
//                        }
//                        
//                        if (operation.equals("exit")) {
//                            break;
//                        }
//                        
//                        String[] data = operation.split("\\+");
//                        
//                        int result = 0;
//                        for (String number : data) {
//                            number = number.trim();
//                            
//                            if (number.isEmpty()) {
//                                continue;
//                            }
//                            
//                            result += Integer.parseInt(number.trim());
//                        }
//                        
//                        output.println(result);
//                        System.out.print("");
//                    } catch (IOException ex) {
//                        Logger.getLogger(TcpServerTest.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    
//                }
//            }
//        });
//        
//        server.expect(":hello", new Action() {
//            @Override
//            public void run(Request request) {
//                System.out.println(request.message());
//            }
//        });
//        
//        TcpClient client = new TcpClient();
//        
//        try {
//            //client.send(":message", InetAddress.getLocalHost().getHostAddress() + ":" + "20000", "Yoooooooooo");
//            
//            client.direct(":calculator", InetAddress.getLocalHost().getHostAddress() + ":" + "20000", new Connection() {
//                public void run() {
//                    String argument = "";
//                    Scanner scanner = new Scanner(System.in);
//                    try {
//                        while (true) {
//                            System.out.println("Sum: ");
//                            argument = scanner.nextLine();
//
//                            ((TcpServer) client.client()).reply(this.socket(), argument.trim());
//                            
//                            if (argument.equals("exit")) {
//                                break;
//                            }
//                            String result = this.input().readLine();
//
//                            System.out.println("Result: " + result);
//                            
//                            System.out.print("");
//                        }
//                        
//                        
//                    } catch (IOException ex) {
//                        Logger.getLogger(TcpServerTest.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            });
//        } catch (UnknownHostException ex) {
//            Logger.getLogger(TcpServerTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        TaskManager tm = new TaskManager();
//        
//        tm.after(5).once(new Task() {
//            @Override
//            public void fire() {
//                Volt.kill(20000);
//                tm.destroy();
//            }
//        });
//    }

    }
}
