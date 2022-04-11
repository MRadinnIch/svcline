package com.svcline.routler;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.svcline.models.Context;
import com.svcline.models.LineResponse;

public interface Routeable {
    void setContext(Context context);

    LineResponse get(Route route, HttpRequest request, HttpResponse response);

    LineResponse put(Route route, HttpRequest request, HttpResponse response);

    LineResponse patch(Route route, HttpRequest request, HttpResponse response);

    LineResponse post(Route route, HttpRequest request, HttpResponse response);

    LineResponse delete(Route route, HttpRequest request, HttpResponse response);
}
