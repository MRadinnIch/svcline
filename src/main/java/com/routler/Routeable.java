package com.routler;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public interface Routeable {
    void setContext(RContext RContext);

    RResponse get(Route route, HttpRequest request, HttpResponse response);

    RResponse put(Route route, HttpRequest request, HttpResponse response);

    RResponse patch(Route route, HttpRequest request, HttpResponse response);

    RResponse post(Route route, HttpRequest request, HttpResponse response);

    RResponse delete(Route route, HttpRequest request, HttpResponse response);
}
