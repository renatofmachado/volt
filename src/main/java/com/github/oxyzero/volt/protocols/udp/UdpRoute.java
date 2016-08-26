package com.github.oxyzero.volt.protocols.udp;

import com.github.oxyzero.volt.Connection;
import com.github.oxyzero.volt.Request;
import com.github.oxyzero.volt.Route;
import com.github.oxyzero.volt.support.Checksum;

import java.util.ArrayList;

public class UdpRoute extends Route {

    private String checksum;

    public UdpRoute(String path, Connection connection) {
        super(path, connection);
    }

    @Override
    public boolean matches(String path) {
        return this.path().equals(path) || path.equals(this.checksum());
    }

    @Override
    public boolean handle(Request request) {
        // We have found an action to perform, but now we need the route arguments.
        // Since we can pass up as much information as possible in a Packet,
        // we'll control the messages using a cyclic route. This means that we
        // need to break the message for each route cycle. For example:
        // Route: :name|:file
        // Message: Students|Students.json|foobar|foobar.xml
        // Gives:
        // Variable :name -> [ Students, foobar ]
        // Variable :file -> [ Students.json, foobar.xml ]

        UdpMessage message = new UdpMessage();
        String delimiter = message.getMessageDelimiter();
        String escaped = "\\" + delimiter;
        String delimiterRegex = message.getMessageDelimiterRegex();

        String[] routeTokens = request.route().split(delimiterRegex);

        if (routeTokens.length == 0) {
            return true;
        }

        String[] requestTokens = request.message().split(delimiterRegex);

        for (int i = 0; i < routeTokens.length; i++) {
            routeTokens[i] = routeTokens[i].substring(1);
            request.put(routeTokens[i], new ArrayList<>());
        }

        int item = 0;

        int length = requestTokens.length / routeTokens.length;

        String cleanedMessage = "";

        for (int i = 0; i < length; i++) {
            for (String routeToken : routeTokens) {
                requestTokens[item] = requestTokens[item].replace(escaped, delimiter);
                cleanedMessage += requestTokens[item] + delimiter;
                request.get(routeToken).add(requestTokens[item++]);
            }

        }

        request.message(cleanedMessage.substring(0, cleanedMessage.length() - delimiter.length()));

        return true;
    }

    @Override
    public void path(String path) {
        super.path(path);
        this.checksum = Checksum.make(this.path());
    }

    /**
     * Gets the checksum of the route's path.
     *
     * @return Checksum of the route's path.
     */
    public String checksum() {
        return this.checksum;
    }

}
