package com.svcline.handlers;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.svcline.Routler.Route;
import com.svcline.Routler.Routeable;
import com.svcline.handlers.db.DbLineFacacde;
import com.svcline.models.Error;
import com.svcline.models.LineItem;
import com.svcline.models.LineResponse;
import com.svcline.models.State;
import com.svcline.prodline.ProductionLine;
import com.svcline.svcline;

import java.util.ArrayList;

import static java.net.HttpURLConnection.*;

public class LineHandler implements Routeable {
    private static final String ITEM_ID = "{itemId}";
    private static final Gson gson = new Gson();
    //private static ProductionLine productionLine = svcline.getProductionLine();

    @Override
    public LineResponse get(Route route, HttpRequest request, HttpResponse response) {
        LineResponse lineResponse;

        if (route.isNaked()) {
            lineResponse = getAll();
        } else {
            lineResponse = getFor(route.getPathVal(ITEM_ID));
        }

        return lineResponse;
    }

    @Override
    public LineResponse put(Route route, HttpRequest request, HttpResponse response) {
        String itemId = route.getPathVal(ITEM_ID);
        if (itemId == null || itemId.isBlank()) {
            return new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Error while updating item due to miss-crafted id.")));
        }

        LineResponse lineResponse;
        ProductionLine productionLine = svcline.getProductionLine();
        try {
            LineItem lineItem = gson.fromJson(request.getReader(), LineItem.class);
            LineItem actualItem = DbLineFacacde.getFor(route.getPathVal(ITEM_ID));

            if (actualItem == null || !actualItem.validate()) { // Check and validate the original item
                lineResponse = new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Failed to obtain/validate original item for id '" + itemId +
                                                                                        "'. Check arguments and try again.")));
            } else if (!lineItem.validate() || !lineItem.getId().equalsIgnoreCase(itemId)) { // Check and validate the "new" item
                lineResponse = new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Failed to update/validate item for id '" + itemId +
                                                                                        "'. Check arguments.")));
            } else if (!productionLine.isInCorrectLineOrder(actualItem, lineItem.getCurrentStationId())) {
                lineResponse = new LineResponse(HTTP_NOT_ACCEPTABLE, gson.toJson(new Error("Item is not in correct production line order. ")));
            } else {
                // Is state transition allowed?
                lineItem.setPreviousStationId(actualItem.getCurrentStationId());
                DbLineFacacde.set(itemId, lineItem);

                lineResponse = new LineResponse(gson.toJson(lineItem));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }

    @Override
    public LineResponse patch(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public LineResponse post(Route route, HttpRequest request, HttpResponse response) {
        LineResponse lineResponse;

        ProductionLine productionLine = svcline.getProductionLine();
        try {
            LineItem lineItem = gson.fromJson(request.getReader(), LineItem.class);
            if (lineItem.getId() == null) {
                lineResponse = new LineResponse(HTTP_NOT_ACCEPTABLE, gson.toJson(new Error("Input item has no id.")));
            } else if (DbLineFacacde.getFor(lineItem.getId()) != null) {
                lineResponse = new LineResponse(HTTP_CONFLICT, gson.toJson(new Error("Failed to create existing object " + lineItem)));
            } else {    // Validations passed, create document
                lineItem.setState(State.START);
                lineItem.setCurrentStationId(productionLine.getStartStationId());
                lineItem.clearPreviousStation();

                DbLineFacacde.set(lineItem.getId(), lineItem);

                lineResponse = new LineResponse(HTTP_CREATED, gson.toJson(lineItem));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;

    }

    @Override
    public LineResponse delete(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    private LineResponse getFor(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Error while looking up unit due to miss-crafted id.")));
        }

        LineResponse lineResponse;
        try {
            LineItem lineItem = DbLineFacacde.getFor(itemId);

            if (lineItem != null) {
                lineResponse = new LineResponse(gson.toJson(lineItem));
            } else {
                lineResponse = new LineResponse(HTTP_NOT_FOUND, gson.toJson(new Error("Item not found for provided id: " + itemId)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }

    private LineResponse getAll() {
        LineResponse lineResponse;
        try {
            ArrayList<LineItem> lineItems = DbLineFacacde.getAll();

            if (lineItems != null) {
                lineResponse = new LineResponse(gson.toJson(lineItems));
            } else {
                lineResponse = new LineResponse(HTTP_NOT_FOUND, gson.toJson(new Error("No items found! Try adding some first ;)")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }
}
