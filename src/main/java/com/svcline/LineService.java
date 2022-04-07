package com.svcline;

import com.svcline.handlers.db.DbLineFacacde;
import com.svcline.models.Error;
import com.svcline.models.LineItem;
import com.svcline.models.LineResponse;
import com.svcline.prodline.ProductionLine;
import com.svcline.prodline.Transition;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static java.net.HttpURLConnection.*;

public class LineService {
    //private static Gson gson = new Gson();

    public static LineResponse put(Transition transition) {
        if (!transition.validate())
            return new LineResponse(HTTP_BAD_REQUEST, new Error("Line transition validation error for: " + transition));

        LineResponse lineResponse;
        String itemId = transition.getId();
        ProductionLine productionLine = svcline.getProductionLine();

        try {
            LineItem lineItemIn = new LineItem(transition);
            LineItem actualItemDb = DbLineFacacde.getFor(itemId);

            /* Basic checks must be performed before we advance to the next station, those being:
             * 1. An item must have previously been created, meaning stored in the DB (with all passed checks previously)
             * 2. The new state the item moves to is provided in the body, which we validate,
             * 3. Body ID must match Path ID. The Path ID is the master!
             */

            if (actualItemDb == null) { // Check and validate the original item
                lineResponse = new LineResponse(HTTP_NOT_FOUND,
                                                new Error("Original item not found for id '" + itemId + "'."));
            } else if (!lineItemIn.validate()) { // Check and validate the "new" item
                lineResponse = new LineResponse(HTTP_BAD_REQUEST,
                                                new Error("Failed to validate item for id '" + itemId + "'. Check arguments."));
            } else if (!lineItemIn.getId().equalsIgnoreCase(itemId)) { // Check and validate the "new" item
                lineResponse = new LineResponse(HTTP_CONFLICT,
                                                new Error("Body ID not matching path ID. Check arguments."));
            } else {
                // We handle the line transition in the production line
                LineItem verifiedItem = productionLine.toNextStation(actualItemDb, lineItemIn);

                DbLineFacacde.set(verifiedItem);

                lineResponse = new LineResponse(verifiedItem);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_CONFLICT, new Error(e.getMessage()));
        } catch (InstantiationException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_UNAVAILABLE, new Error(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, new Error(e.getMessage()));
        }

        return lineResponse;
    }

    public static LineResponse create(Transition transition) {
        // When we create a transition we're only interested in the ID.
        if (transition.getId() == null)
            return new LineResponse(HTTP_BAD_REQUEST, new Error("Line initiation validation error for: " + transition));

        LineResponse lineResponse;
        String itemId = transition.getId();
        ProductionLine productionLine = svcline.getProductionLine();

        try {
            if (DbLineFacacde.getFor(itemId) != null) {
                lineResponse = new LineResponse(HTTP_CONFLICT, new Error("Failed to create existing object for existing id: " + itemId));
            } else {    // Validations passed, create document
                LineItem lineItem = productionLine.startProduction(itemId);

                DbLineFacacde.set(lineItem);

                lineResponse = new LineResponse(HTTP_CREATED, lineItem);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_UNAVAILABLE, new Error(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, new Error(e.getMessage()));
        }

        return lineResponse;
    }

    public static LineResponse getItem(String itemId) {
        if (itemId == null)
            return new LineResponse(HTTP_BAD_REQUEST, new Error("itemId cannot be null!"));

        LineResponse lineResponse;
        try {
            LineItem lineItem = DbLineFacacde.getFor(itemId);

            if (lineItem != null) {
                lineResponse = new LineResponse(lineItem);
            } else {
                lineResponse = new LineResponse(HTTP_NOT_FOUND, new Error("Item not found for provided id: " + itemId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, new Error(e.getMessage()));
        }

        return lineResponse;
    }

    public static LineResponse getAllItems() {
        LineResponse lineResponse;
        try {
            ArrayList<LineItem> lineItems = DbLineFacacde.getAll();

            if (lineItems != null) {
                lineResponse = new LineResponse(lineItems);
            } else {
                lineResponse = new LineResponse(HTTP_NOT_FOUND, new Error("No items found! Try adding some first ;)"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, new Error(e.getMessage()));
        }

        return lineResponse;
    }
}
