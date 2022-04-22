package com.svcline.handlers;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.svcline.LineService;
import com.routler.RContext;
import com.routler.RError;
import com.routler.RResponse;
import com.svcline.prodline.Transition;
import com.routler.Route;
import com.routler.Routeable;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_IMPLEMENTED;

public class LineHandler implements Routeable {
    private static final String ITEM_ID = "{itemId}";
    private static final Gson gson = new Gson();
    private RContext RContext;

    @Override
    public void setContext(RContext RContext) {
        this.RContext = RContext;
    }

    @Override
    public RResponse get(Route route, HttpRequest request, HttpResponse response) {
        RResponse rResponse;

        if (route.isNaked()) {
            rResponse = getAll();
        } else {
            rResponse = getFor(route.getPathVal(ITEM_ID));
        }

        return rResponse;
    }

    @Override
    public RResponse put(Route route, HttpRequest request, HttpResponse response) {
        String itemId = route.getPathVal(ITEM_ID);
        if (itemId == null || itemId.isBlank()) {
            return new RResponse(HTTP_BAD_REQUEST, new RError("Error while updating item due to miss-crafted id."));
        }

        Transition transition;

        try {
            transition = gson.fromJson(request.getReader(), Transition.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new RResponse(HTTP_BAD_REQUEST, new RError("Unexpected error while updating item. Please try again."));
        }

        LineService lineService = new LineService(RContext.getProductionLine());
        return lineService.stationItemStop(transition);
    }

    @Override
    public RResponse patch(Route route, HttpRequest request, HttpResponse response) {
        String itemId = route.getPathVal(ITEM_ID);
        if (itemId == null || itemId.isBlank()) {
            return new RResponse(HTTP_BAD_REQUEST, new RError("Error while updating item due to miss-crafted id."));
        }

        Transition transition;

        try {
            transition = gson.fromJson(request.getReader(), Transition.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new RResponse(HTTP_BAD_REQUEST, new RError("Unexpected error while updating item. Please try again."));
        }

        LineService lineService = new LineService(RContext.getProductionLine());

        try {
            lineService.stationItemStart(transition);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return new RResponse(HTTP_BAD_REQUEST, new RError("Item not prepared for processing please prepare."));
        }

        return new RResponse("Item prepared for production.");
    }

    @Override
    public RResponse post(Route route, HttpRequest request, HttpResponse response) {
        Transition transition;

        try {
            transition = gson.fromJson(request.getReader(), Transition.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new RResponse(HTTP_BAD_REQUEST, new RError("Unexpected error while creating item. Please try again."));
        }

        LineService lineService = new LineService(RContext.getProductionLine());
        return lineService.startProduction(transition);
    }

    @Override
    public RResponse delete(Route route, HttpRequest request, HttpResponse response) {
        return new RResponse(HTTP_NOT_IMPLEMENTED, new RError("DELETE method not implemented"));
    }

    private RResponse getFor(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return new RResponse(HTTP_BAD_REQUEST, new RError("Error while looking up unit due to miss-crafted id."));
        }

        LineService lineService = new LineService(RContext.getProductionLine());
        return lineService.getItem(itemId);
    }

    private RResponse getAll() {
        LineService lineService = new LineService(RContext.getProductionLine());
        return lineService.getAllItems();
    }
}
