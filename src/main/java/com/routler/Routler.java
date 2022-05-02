package com.routler;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.svcline.models.clocker.bq.BigQueryService;

import java.util.logging.Logger;
import java.util.ArrayList;

import static java.net.HttpURLConnection.HTTP_NOT_IMPLEMENTED;

// Route handler
public class Routler {
    private static final Logger logger = Logger.getLogger(Routler.class.getName());
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
                logger.info("Handler class already exists!");
                return true;
            }
        }

        return false;
    }

    private static boolean routeExists(String path) {
        if (routes.size() == 0)
            return false;

        for (Route route : routes) {
            if (route.equalsTo(path)) {
                logger.info("Route already exists, failed to register!!!");
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public static void listRoutes() {
        logger.info("Registered routes:");
        for (Route route : routes) {
            logger.info(route.toString());
        }
        logger.info("\n");
    }

    public static RResponse handle(HttpRequest request, HttpResponse response, RContext RContext) {
        for (Route route : routes) {
            // If we've found our route, and it can be executed. Execute!
            if (route.equalsTo(request.getPath())) {
                return route.execute(route, request, response, RContext);
            }
        }

        return new RResponse(HTTP_NOT_IMPLEMENTED, new RError("method '" + request.getMethod() + "' not implemented."));
    }
}
