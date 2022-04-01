package com.svcline.handlers;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.svcline.handlers.db.DbLineFacacde;
import com.svcline.models.Error;
import com.svcline.models.LineItem;
import com.svcline.models.LineResponse;
import com.svcline.prodline.ProductionLine;
import com.svcline.routler.Route;
import com.svcline.routler.Routeable;
import com.svcline.svcline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static java.net.HttpURLConnection.*;

public class LineHandler implements Routeable {
    private static final String ITEM_ID = "{itemId}";
    private static final Gson gson = new Gson();

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
            LineItem lineItemIn = new LineItem(request);
            LineItem actualItemDb = DbLineFacacde.getFor(itemId);

            /* Basic checks must be performed before we advance to the next station, those being:
             * 1. An item must have previously been created, meaning stored in the DB (with all passed checks previously)
             * 2. The new state the item moves to is provided in the body, which we validate,
             * 3. Body ID must match Path ID. The Path ID is the master!
             */

            if (actualItemDb == null) { // Check and validate the original item
                lineResponse = new LineResponse(HTTP_NOT_FOUND,
                                                gson.toJson(new Error("Original item not found for id '" + itemId + "'.")));
            } else if (!lineItemIn.validate()) { // Check and validate the "new" item
                lineResponse = new LineResponse(HTTP_BAD_REQUEST,
                                                gson.toJson(new Error("Failed to validate item for id '" + itemId + "'. Check arguments.")));
            } else if (!lineItemIn.getId().equalsIgnoreCase(itemId)) { // Check and validate the "new" item
                lineResponse = new LineResponse(HTTP_CONFLICT,
                                                gson.toJson(new Error("Body ID not matching path ID. Check arguments.")));
            } else {
                // We handle the line transition in the production line
                LineItem verifiedItem = productionLine.toNextStation(actualItemDb, lineItemIn);

                DbLineFacacde.set(verifiedItem);

                lineResponse = new LineResponse(gson.toJson(verifiedItem));

            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_CONFLICT, gson.toJson(new Error(e.getMessage())));
        } catch (InstantiationException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_UNAVAILABLE, gson.toJson(new Error(e.getMessage())));
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }

    @Override
    public LineResponse patch(Route route, HttpRequest request, HttpResponse response) {
        return new LineResponse(HTTP_NOT_IMPLEMENTED, gson.toJson(new Error("PUT method not implemented")));
    }

    @Override
    public LineResponse post(Route route, HttpRequest request, HttpResponse response) {
        LineResponse lineResponse;

        ProductionLine productionLine = svcline.getProductionLine();
        try {
            String id = gson.fromJson(request.getReader(), LineItem.class).getId();
            if (id == null) {
                lineResponse = new LineResponse(HTTP_NOT_ACCEPTABLE, gson.toJson(new Error("Input item has no id.")));
            } else if (DbLineFacacde.getFor(id) != null) {
                lineResponse = new LineResponse(HTTP_CONFLICT, gson.toJson(new Error("Failed to create existing object for existing id: " + id)));
            } else {    // Validations passed, create document
                LineItem lineItem = productionLine.startProduction(id);

                DbLineFacacde.set(lineItem);

                lineResponse = new LineResponse(HTTP_CREATED, gson.toJson(lineItem));
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_UNAVAILABLE, gson.toJson(new Error(e.getMessage())));
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;

    }

    @Override
    public LineResponse delete(Route route, HttpRequest request, HttpResponse response) {
        return new LineResponse(HTTP_NOT_IMPLEMENTED, gson.toJson(new Error("DELETE method not implemented")));
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
