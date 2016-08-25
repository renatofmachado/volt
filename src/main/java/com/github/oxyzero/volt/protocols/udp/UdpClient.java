package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Client;
import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.support.RequestBuilder;
import com.github.oxyzero.volt.support.Task;

import java.util.function.Consumer;

/**
 * This class represents a client that allows to quickly send a message to
 * a Volt server instance.
 * 
 * @author Renato Machado
 */
public class UdpClient extends Client {
    
    /**
     * Creates a new UDP Client using a dynamic port.
     */
    public UdpClient()
    {
        super(new UdpServer(), 0);
    }
    
    /**
     * Creates a new UDP Client.
     *
     * @param port Client port. Use 0 to use a dynamic port.
     */
    public UdpClient(int port)
    {
        super(new UdpServer(), port);
    }

    @Override
    public Client send(String route, String target, Connection connection) {
        Task request = new Task() {
            @Override
            public void fire() {
                RequestBuilder requestBuilder = new RequestBuilder();
                requestBuilder.message("");
                requestBuilder.route(route);
                requestBuilder.target(target);

                Request request = requestBuilder.build();

                connection.onEnter();
                connection.run(request);
                connection.onExit();
            }
        };

        super.fire(request);

        return this;
    }

    @Override
    public Client send(String headers, String target, Consumer<Request> action) {
        Connection connection = new Connection() {
            @Override
            public void run(Request request) {
                action.accept(request);
            }
        };

        return this.send(headers, target, connection);
    }

    @Override
    public Client reply(Request request, Object response) {
        if (! (response instanceof String)) {
            throw new IllegalArgumentException("The response must be a string object.");
        }

        this.send(request.route(), request.requester().target(), (String) response);

        return this;
    }
}
