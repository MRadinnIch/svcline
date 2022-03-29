package com.svcline.Routler;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

public interface Routeable {
    String get(Route route, HttpRequest request, HttpResponse response);

    String put(Route route, HttpRequest request, HttpResponse response);

    String patch(Route route, HttpRequest request, HttpResponse response);

    String post(Route route, HttpRequest request, HttpResponse response);

    String delete(Route route, HttpRequest request, HttpResponse response);
}
