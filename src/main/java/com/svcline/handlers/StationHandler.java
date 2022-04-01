package com.svcline.handlers;

import com.svcline.prodline.ProductionLine;
import com.svcline.routler.Route;
import com.svcline.routler.Routeable;
import com.svcline.models.Error;
import com.svcline.models.LineResponse;
import com.svcline.svcline;
import com.svcline.models.Unit;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.*;

public class StationHandler implements Routeable {
    private static final String COLLECTION = "units";
    private static final Gson gson = new Gson();

    private LineResponse getAll() {
        LineResponse lineResponse;
        try {
            Firestore db = svcline.getFirestore();

            ApiFuture<QuerySnapshot> query = db.collection(COLLECTION).get();

            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            ArrayList<Unit> units = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                units.add(document.toObject(Unit.class));
            }

            lineResponse = new LineResponse(gson.toJson(units));
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }

    private LineResponse getFor(String jbId) {
        if (jbId == null || jbId.isBlank()) {
            return new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Error while looking up unit due to miss-crafted id.")));
        }

        LineResponse lineResponse;
        try {
            Firestore db = svcline.getFirestore();

            ApiFuture<DocumentSnapshot> query = db.collection(COLLECTION).document(jbId).get();

            DocumentSnapshot document = query.get();
            Unit unit = document.toObject(Unit.class);

            lineResponse = new LineResponse(gson.toJson(unit));
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }

    public LineResponse get(Route route, HttpRequest request, HttpResponse response) {
        LineResponse lineResponse;

        if (route.isNaked()) {
            lineResponse = getAll();
        } else {
            lineResponse = getFor(route.getPathVal("{jbId}"));
        }

        return lineResponse;
    }

    @Override
    public LineResponse put(Route route, HttpRequest request, HttpResponse response) {
        String jbId = route.getPathVal("{jbId}");
        if (jbId == null || jbId.isBlank()) {
            return new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Error while PUT due to miss-crafted id.")));
        }

        LineResponse lineResponse;
        try {
            Firestore db = svcline.getFirestore();

            Unit unit = gson.fromJson(request.getReader(), Unit.class);
            if (unit.validate() && unit.getJbId().equalsIgnoreCase(jbId)) {
                db.collection(COLLECTION).document(jbId).set(unit);

                lineResponse = new LineResponse(gson.toJson(unit));
            } else {
                lineResponse = new LineResponse(HTTP_BAD_REQUEST, gson.toJson(new Error("Failed to PUT for id " + jbId)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;
    }

    @Override
    public LineResponse patch(Route route, HttpRequest request, HttpResponse response) {
        svcline.reloadProductionLineConfiguration();
        return new LineResponse(HTTP_ACCEPTED, gson.toJson(new Error("Production line configuration reloaded")));
    }

    @Override
    public LineResponse post(Route route, HttpRequest request, HttpResponse response) {
        LineResponse lineResponse;

        try {
            Firestore db = svcline.getFirestore();

            Unit unit = gson.fromJson(request.getReader(), Unit.class);
            if(!unit.validate()) {
                lineResponse = new LineResponse(HTTP_NOT_ACCEPTABLE, gson.toJson(new Error("Failed to validate input object " + unit)));
            }
            else if (getFor(unit.getJbId()) != null) {
                lineResponse = new LineResponse(HTTP_CONFLICT, gson.toJson(new Error("Failed to create existing object " + unit)));
            } else {    // Validations passed, create document
                db.collection(COLLECTION).document(unit.getJbId()).set(unit);
                lineResponse = new LineResponse(HTTP_CREATED, gson.toJson(unit));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lineResponse = new LineResponse(HTTP_INTERNAL_ERROR, gson.toJson(new Error(e.getMessage())));
        }

        return lineResponse;

    }

    @Override
    public LineResponse delete(Route route, HttpRequest request, HttpResponse response) {
        return new LineResponse(HTTP_NOT_FOUND, gson.toJson(new Error("DELETE not implemented yet!")));
    }
}

/*
            try {
                URL url = new URL(URL_JETBODIES);

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                int status = con.getResponseCode();

                Reader streamReader = null;

                if (status > 299) {
                    streamReader = new InputStreamReader(con.getErrorStream());
                } else {
                    streamReader = new InputStreamReader(con.getInputStream());
                }

                BufferedReader in = new BufferedReader(streamReader);
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                //System.out.println(content);

                json = content.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
 */