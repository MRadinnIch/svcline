package com.svcline.handlers;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.routler.*;
import com.svcline.prodline.ProductLineConfiguration;
import com.svcline.prodline.db.DbProdLineConfiguration;

import java.util.logging.Logger;

import static java.net.HttpURLConnection.*;

public class ConfigurationHandler implements Routeable {
    private static final String ITEM_ID = "{configId}";
    private RContext RContext;

    @Override
    public void setContext(RContext RContext) {
        this.RContext = RContext;
    }

    @Override
    public RResponse get(Route route, HttpRequest request, HttpResponse response) {
        RResponse rResponse;

        if (!route.isNaked()) {
            rResponse = getFor(route.getPathVal(ITEM_ID));
        } else {
            return new RResponse(HTTP_BAD_REQUEST, new RError("Provide config ID which you want."));
        }

        return rResponse;
    }

    private RResponse getFor(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return new RResponse(HTTP_BAD_REQUEST, new RError("Error while looking up configuration due to miss-crafted id."));
        }

        RResponse rResponse;
        try {
            DbProdLineConfiguration dbProdLineConfiguration = new DbProdLineConfiguration(RContext.getProductionLine().getFirestore());
            ProductLineConfiguration plc = dbProdLineConfiguration.read(itemId);

            if (plc != null) {
                rResponse = new RResponse(plc.getConfiguredStationMap());
            } else {
                rResponse = new RResponse(HTTP_NOT_FOUND, new RError("Configuration not found for provided id: " + itemId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            rResponse = new RResponse(HTTP_INTERNAL_ERROR, new RError(e.getMessage()));
        }

        return rResponse;
    }

    @Override
    public RResponse put(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public RResponse patch(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public RResponse post(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public RResponse delete(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }
}
