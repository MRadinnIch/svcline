package com.svcline;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.svcline.models.Error;
import com.svcline.routler.Routler;
import com.svcline.handlers.LineHandler;
import com.svcline.handlers.StationHandler;
import com.svcline.models.*;
import com.svcline.prodline.ProductionLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class svcline implements HttpFunction {
    private static Firestore firestore = null;
    private static ProductionLine productionLine = null;
    private static final Gson gson = new Gson();

    private static final String CONTENT_TYPE = "application/json;charset=utf-8";
    private static final String SERVICE_ACCOUNT = "radinn-rindus-firebase-adminsdk-ha599-fbeceb59df.json";
    private static final String PROJECT_ID = "radinn-rindus";

    private static final String PATH_JETBODIES = "/jetbodies/{jbId}";
    private static final String PATH_PRODUCTIONLINE = "/items/{itemId}";
    private static final String TEST_PATH = "/test/{test}/pest/{pest}/rest";
    private static final String BEST_PATH = "/test/{test}/pest/{pest}/rest/{rest}/best/";

    // Register our path with handlers
    static {
        Routler.register(PATH_JETBODIES, new StationHandler());
        Routler.register(PATH_PRODUCTIONLINE, new LineHandler());
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        initFirestore();
        initProductionLine();

        LineResponse lineResponse = Routler.handle(request.getMethod(), request.getPath(), request, response);

        response.setContentType(CONTENT_TYPE);
        OutputStream os = response.getOutputStream();   // Must use OutputStream for UTF-8

        if (lineResponse != null) {
            response.setStatusCode(lineResponse.getCode());
            os.write(bytes(lineResponse.getJson()));
        } else {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            os.write(bytes(gson.toJson(new Error("Unexpected error while handling request."))));
        }
    }

    private void initFirestore() {
        if (firestore == null) {
            try {
                InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(SERVICE_ACCOUNT);

                assert serviceAccount != null;
                FirestoreOptions firestoreOptions =
                        FirestoreOptions.getDefaultInstance().toBuilder()
                                .setProjectId(PROJECT_ID)
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();

                firestore = firestoreOptions.getService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initProductionLine() {
        if(productionLine == null) {
            HashMap<String, Station> stationMap = new HashMap<>();
            Station station1 = new Station("1001", "Start Station", StationType.START,
                                           new ArrayList<>(List.of(State.PASS, State.FAIL)));

            Station station2 = new Station("1002", "Second Station", StationType.PRODUCTION,
                                           new ArrayList<>(List.of(State.PASS, State.FAIL)));

            Station station3 = new Station("1003", "Third Station", StationType.PRODUCTION,
                                           new ArrayList<>(List.of(State.PASS, State.FAIL)));

            Station station4 = new Station("1004", "End Station", StationType.END,
                                           new ArrayList<>(List.of(State.PASS, State.FAIL)));

            Station serviceStation = new Station("2001", "Service Station", StationType.SERVICE,
                                                 new ArrayList<>(List.of(State.PASS, State.RETRY, State.SCRAP)));
            stationMap.put(station1.getId(), station1);
            stationMap.put(station2.getId(), station2);
            stationMap.put(station3.getId(), station3);
            stationMap.put(station4.getId(), station4);
            stationMap.put(serviceStation.getId(), serviceStation);

            HashMap<String, String> stationOrder = new HashMap<>();
            stationOrder.put(station1.getId(), station2.getId());
            stationOrder.put(station2.getId(), station3.getId());
            stationOrder.put(station3.getId(), station4.getId());

            productionLine = new ProductionLine();
            try {
                productionLine.init(stationMap, stationOrder);
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public static ProductionLine getProductionLine(){
        return productionLine;
    }

    public static Firestore getFirestore() {
        return firestore;
    }

    private static byte[] bytes(String str) {
        return str != null ? str.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8);
    }
}