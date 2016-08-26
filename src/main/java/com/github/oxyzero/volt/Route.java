package com.github.oxyzero.volt;

public abstract class Route {

    private String path;

    private Connection connection;

    public Route(String path, Connection connection) {
        this.path(path);
        this.connection = connection;
    }

    /**
     * Check if the given path matches this route's path.
     *
     * @param path Route identifier.
     * @return True if the routes match, false otherwise.
     */
    public abstract boolean matches(String path);

    /**
     * Handles the routes arguments.
     *
     * @param request Request.
     * @return True if the route can be handled correctly, false otherwise.
     */
    public abstract boolean handle(Request request);

    /**
     * Executes the route.
     *
     * @param request Request.
     */
    public synchronized void run(Request request)
    {
        this.connection.run(request);
    }

    /**
     * Gets the path of the route.
     *
     * @return Path of the route.
     */
    public String path() {
        return this.path;
    }

    /**
     * Sets the path of the route.
     */
    public void path(String path) {
        this.path = this.clean(path);
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
