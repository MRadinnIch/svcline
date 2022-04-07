package com.svcline.handlers;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.svcline.models.Error;
import com.svcline.models.LineResponse;
import com.svcline.prodline.ProductLineConfiguration;
import com.svcline.prodline.db.DbProdLineConfiguration;
import com.svcline.routler.Route;
import com.svcline.routler.Routeable;

import static java.net.HttpURLConnection.*;

public class ConfigurationHandler implements Routeable {
    private static final String ITEM_ID = "{configId}";

    @Override
    public LineResponse get(Route route, HttpRequest request, HttpResponse response) {
        LineResponse lineResponse;

        if (!route.isNaked()) {
            lineResponse = getFor(route.getPathVal(ITEM_ID));
        } else {
            return new LineResponse(HTTP_BAD_REQUEST, new Error("Provide config ID which you want."));
        }

        return lineResponse;
    }

    private LineResponse getFor(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return new LineResponse(HTTP_BAD_REQUEST, new Error("Error while looking up configuration due to miss-crafted id."));
        }

        LineResponse lineResponse;
        try {
            ProductLineConfiguration plc = DbProdLineConfiguration.read(itemId);

            if (plc != null) {
                lineResponse = new LineResponse(plc.getConfiguredStationMap());
            } else {
                lineResponse = new LineResponse(HTTP_NOT_FOUND, new Error("Configuration not found for provided id: " + itemId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, new Error(e.getMessage()));
        }

        return lineResponse;
    }

    @Override
    public LineResponse put(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public LineResponse patch(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public LineResponse post(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public LineResponse delete(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }
}
