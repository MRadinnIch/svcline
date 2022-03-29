package com.svcline.handlers;

import com.svcline.Routler.Route;
import com.svcline.Routler.Routeable;
import com.svcline.svcline;
import com.svcline.prodline.Unit;
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

public class StationHandler implements Routeable {
    private static final String COLLECTION = "units";
    private static final Gson gson = new Gson();

    private String getAll() {
        String json = "";

        try {
            Firestore db = svcline.getFirestore();

            // asynchronously retrieve all units
            ApiFuture<QuerySnapshot> query = db.collection(COLLECTION).get();
            //db.collection("").document("").get();

            QuerySnapshot querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            ArrayList<Unit> units = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                units.add(document.toObject(Unit.class));
            }

            json = gson.toJson(units);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    private String getFor(String jbId) {
        if (jbId == null || jbId.isBlank()) {
            return null;
        }

        String json = null;

        try {
            Firestore db = svcline.getFirestore();

            ApiFuture<DocumentSnapshot> query = db.collection(COLLECTION).document(jbId).get();

            DocumentSnapshot document = query.get();
            Unit unit = document.toObject(Unit.class);

            json = gson.toJson(unit);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    public String get(Route route, HttpRequest request, HttpResponse response) {
        System.out.println("StationHandler.get: " + request.getPath());
        String json;

        if (route.isNaked()) {
            json = getAll();
        } else {
            json = getFor(route.getPathVal("{jbId}"));
        }

        return json;
    }

    @Override
    public String put(Route route, HttpRequest request, HttpResponse response) {
        String jbId = route.getPathVal("{jbId}");
        if (jbId == null || jbId.isBlank()) {
            return null;
        }

        String json = null;
        try {
            Firestore db = svcline.getFirestore();

            Unit unit = gson.fromJson(request.getReader(), Unit.class);
            if (unit.validate() && unit.getJbId().equalsIgnoreCase(jbId)) {
                db.collection(COLLECTION).document(jbId).set(unit);

                json = gson.toJson(unit);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    @Override
    public String patch(Route route, HttpRequest request, HttpResponse response) {
        return null;
    }

    @Override
    public String post(Route route, HttpRequest request, HttpResponse response) {

        String json = null;
        /*try {
            Firestore db = gcptest.getFirestore();

            Unit unit = gson.fromJson(request.getReader(), Unit.class);
            if (unit.validate()) {
                //db.collection(COLLECTION).document().create().
                db.collection(COLLECTION).document(unit.getJbId()).set(unit);

                json = gson.toJson(unit);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return json;

    }

    @Override
    public String delete(Route route, HttpRequest request, HttpResponse response) {
        return null;
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