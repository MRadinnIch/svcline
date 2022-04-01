package com.svcline;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.svcline.handlers.LineHandler;
import com.svcline.handlers.StationHandler;
import com.svcline.models.Error;
import com.svcline.models.LineResponse;
import com.svcline.prodline.ProductLineConfiguration;
import com.svcline.prodline.ProductionLine;
import com.svcline.routler.Routler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class svcline implements HttpFunction {
    private static Firestore firestore = null;
    private static ProductionLine productionLine = null;
    private static final Gson gson = new Gson(); //GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private static final String CONTENT_TYPE = "application/json;charset=utf-8";
    private static final String SERVICE_ACCOUNT = "radinn-rindus-firebase-adminsdk-ha599-fbeceb59df.json";
    private static final String PROJECT_ID = "radinn-rindus";

    private static final String PATH_JET_BODIES = "/jetbodies/config";
    private static final String PATH_PRODUCTION_LINE = "/items/{itemId}";

    // Register our path with handlers
    static {
        Routler.register(PATH_JET_BODIES, new StationHandler());
        Routler.register(PATH_PRODUCTION_LINE, new LineHandler());
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        OutputStream os;   // Must use OutputStream for UTF-8
        try {
            // No need to continue execution if we fail to return the response.
            os = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        response.setContentType(CONTENT_TYPE);

        // Since we can return the response, now we initiate the system and gracefully exit if it fails.
        try {
            initFirestore();
            initProductionLine();
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            try {
                os.write(bytes(gson.toJson(new Error("Production line initialization failed. Application will not start."))));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }

        // We came so far, now handle the request
        LineResponse lineResponse = Routler.handle(request, response);

        // Attempt returning the actual response
        try {
            if (lineResponse != null) {
                response.setStatusCode(lineResponse.getCode());
                os.write(bytes(lineResponse.getJson()));
            } else {
                response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                os.write(bytes(gson.toJson(new Error("Unexpected error while handling request."))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFirestore() throws IOException {
        if (firestore == null) {
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(SERVICE_ACCOUNT);

            assert serviceAccount != null;
            FirestoreOptions firestoreOptions =
                    FirestoreOptions.getDefaultInstance().toBuilder()
                            .setProjectId(PROJECT_ID)
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();

            firestore = firestoreOptions.getService();
        }
    }

    private void initProductionLine() throws InstantiationException {
        if (productionLine == null) {
            productionLine = new ProductionLine();

            //ProductLineConfiguration productLineConfiguration = ProductLineConfiguration.loadFromDb();
            ProductLineConfiguration productLineConfiguration = new  ProductLineConfiguration();
            productLineConfiguration.loadTestConfiguration();
            productLineConfiguration.writeToDb();

            productionLine.init(productLineConfiguration.getConfiguredStationMap(), productLineConfiguration.getConfiguredStationOrder());
        }
    }

    public static void reloadProductionLineConfiguration() {
        ProductLineConfiguration cfg = ProductLineConfiguration.loadFromDb("demo2-configuration");
        try {
            productionLine.init(cfg.getConfiguredStationMap(), cfg.getConfiguredStationOrder());
            System.out.println("Production line setup:\n" + gson.toJson(cfg));
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static ProductionLine getProductionLine() {
        return productionLine;
    }

    public static Firestore getFirestore() {
        return firestore;
    }

    private static byte[] bytes(String str) {
        return str != null ? str.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8);
    }
}