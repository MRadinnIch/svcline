package com.svcline.routler;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.svcline.models.LineResponse;

import java.net.HttpURLConnection;
import java.util.ArrayList;

// Route handler
public class Routler {
    private static final ArrayList<Route> routes = new ArrayList<>();

    public Routler() {
    }

    public static void register(String path, Routeable handler) {
        if (path == null || handler == null)
            throw new RuntimeException("Input parameter cannot be null");
        else if (routeExists(path))
            throw new RuntimeException("Route already registered for path: " + path);
        else if (handlerExists(handler))
            throw new RuntimeException("Handler already registered for path: " + path);

        Route route = new Route(path, handler);

        routes.add(route);
    }

    private static boolean handlerExists(Routeable handler) {
        for (Route route : routes) {
            if (route.getHandler().getClass().equals(handler.getClass())) {
                System.out.println("Handler class already exists!");
                return true;
            }
        }

        return false;
    }

    private static boolean routeExists(String path) {
        if (routes.size() == 0)
            return false;

        for (Route route : routes) {
            //if (method.equalsIgnoreCase(route.getMethod()) && route.equalsTo(path)) {
            if (route.equalsTo(path)) {
                System.out.println("Route already exists, failed to register!!!");
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public static void listRoutes() {
        System.out.println("Registered routes:");
        for (Route route : routes) {
            System.out.println(route.toString());
        }
        System.out.println("\n");
    }

    public static LineResponse handle(String method, String path, HttpRequest request, HttpResponse response) {
        for (Route route : routes) {
            // If we've found our route, and it can be executed. Execute!
            //if (route.equalsTo(path) && route.isExecutable(method)) {
            if (route.equalsTo(path)) {
                return route.execute(route, request, response);
            }
        }

        return new LineResponse(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "{\"method\" : \"" + method + "\", \"status\" : \"Not implemented\"}");
    }
}
