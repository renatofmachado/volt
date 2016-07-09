package com.github.oxyzero.volt.protocols.tcp;

import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.Volt;

import java.net.UnknownHostException;

public class TcpClientTest {

    public static void main(String[] args) throws UnknownHostException {
        TcpClient client = new TcpClient();
        
        client.direct(":chat", Volt.localhost(20000), new Connection() {
            
            public boolean canDisconnect() {
                return false;
            }
            
            @Override
            public void run(Request request) {
                
            }
            
        });
    }
    
}
