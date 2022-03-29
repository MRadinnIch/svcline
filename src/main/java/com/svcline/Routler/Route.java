package com.svcline.Routler;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;

public class Route {
    private final ArrayList<Pair> routePairList;
    private final String route;
    private final Routeable handler;
    private HashMap<String, String> vMap = null;    // Null and set only if configured and called route match

    public Route(String route, Routeable handler) {
        this.route = route;
        this.handler = handler;
        this.routePairList = mapRoutePairs(route);
    }

    private ArrayList<Pair> mapRoutePairs(String route) {
        if (route.length() == 1)
            return null;

        String path = route.substring(1);
        String[] routeArr = path.split("/");

        if (routeArr.length == 0)
            return null;

        int i = 0;
        ArrayList<Pair> pairs = new ArrayList<>();
        while (i < routeArr.length) {
            String k = !routeArr[i].isEmpty() ? routeArr[i] : "";
            String v = i != routeArr.length - 1 ? routeArr[i + 1] : "";

            pairs.add(new Pair(k, v));
            i = i + 2;
        }

        return pairs;
    }

    public String execute(Route route, HttpRequest request, HttpResponse response) {
        switch (request.getMethod()) {
            case "GET":
                return handler.get(route, request, response);

            case "PUT":
                return handler.put(route, request, response);

            case "PATCH":
                return handler.patch(route, request, response);

            case "POST":
                return handler.post(route, request, response);

            case "DELETE":
                return handler.delete(route, request, response);

            default:
                return null;
        }
    }

    public String getPathVal(String pathVar) {
        return (pathVar == null || this.vMap == null) ? null : this.vMap.get(pathVar);
    }

    // Checks whether a registered route equals an invoked route path.
    // If equal the invoked path's variables will be mapped to the
    // registered route's path parameters so that they can be retrieved
    // via @getPathVal.
    public boolean equalsTo(String routeStr) {
        this.vMap = new HashMap<>();
        ArrayList<Pair> pairs = mapRoutePairs(routeStr);

        if (pairs == null)
            return false;

        if (this.routePairList.size() != pairs.size())
            return false;
        else if (pairs.size() == 0)
            return true;

        int i = 0;
        int match = 0;
        for (; i < pairs.size(); i++) {
            Pair regPair = this.routePairList.get(i);
            Pair inPair = pairs.get(i);

            if (inPair.getK().equalsIgnoreCase(regPair.getK())) {
                match++;

                //
                if(!regPair.getV().isEmpty() && !inPair.getV().isEmpty())
                    this.vMap.put(regPair.getV(), inPair.getV());
            }
        }

        if (i != match)
            this.vMap = null;

        return i == match;
    }

    public String getRouteValue(String vPath){
        if(vPath == null || vPath.isEmpty() || vPath.isBlank() || this.vMap == null)
            return null;

        return this.vMap.get(vPath);
    }

    public boolean isNaked() {
        return this.vMap == null || this.vMap.size() == 0;
    }

    public Routeable getHandler() {
        return this.handler;
    }
}
