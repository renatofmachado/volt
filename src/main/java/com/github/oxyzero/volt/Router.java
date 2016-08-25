package com.github.oxyzero.volt;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Router {

    private Map<String, Route> routes;

    public Router()
    {
        this.routes = new HashMap<>();
    }

    /**
     * Registers a route.
     *
     * @param path Route identifier.
     * @param route Route instance.
     */
    public void register(String path, Route route)
    {
        this.routes.put(this.clean(path), route);
    }

    /**
     * Resolves the identity of the route.
     *
     * @return Route path identifier.
     */
    public String resolve(final String id)
    {
        final String route = this.clean(id);

        if (this.routes.containsKey(route)) {
            return route;
        }

        return this.routes().filter(r -> r.matches(route))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("The router couldn't resolve the route identification because it is not registered on the server."))
                .path();
    }

    /**
     * Removes a route.
     *
     * @param path Route identifier.
     */
    public void remove(String path)
    {
        this.routes.remove(this.clean(path));
    }

    /**
     * Gets all of the registered routes.
     *
     * @return Registered routes.
     */
    public Stream<Route> routes()
    {
        return this.routes.values().stream();
    }

    /**
     * Handles a request by executing the route with the given path.
     *
     * @param path Route identifier.
     */
    public void handle(String path, Request request)
    {
        final String routePath = this.clean(path);

        if (this.routes.containsKey(routePath)) {
            Route route = this.routes.get(routePath);

            if (! route.matches(routePath) || ! route.handle(request)) {
                throw new IllegalArgumentException("No route was a match for the given path.");
            }
        }

        this.routes()
                .filter(r -> r.matches(routePath) && r.handle(request))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The server does not recognize the given route."))
                .run(request);
    }

    /**
     * Checks if the router contains the given route identifier.
     *
     * @param path Route identifier.
     * @return True if the route exists, false otherwise.
     */
    public boolean has(String path)
    {
        final String route = this.clean(path);

        if (this.routes.containsKey(route)) {
            return true;
        }

        return this.routes()
                .filter(r -> r.matches(route))
                .findAny()
                .isPresent();
    }

    /**
     * Sanitizes the route's path.
     *
     * @param path Route identifier.
     * @return Sanitized route identifier.
     */
    private String clean(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1, path.length());
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }
}
