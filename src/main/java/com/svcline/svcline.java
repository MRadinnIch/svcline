package com.svcline;

import com.svcline.Routler.Routler;
import com.svcline.handlers.StationHandler;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class svcline implements HttpFunction {
    private static Firestore firestore = null;

    private static final String CONTENT_TYPE = "application/json;charset=utf-8";
    private static final String SERVICE_ACCOUNT = "radinn-rindus-firebase-adminsdk-ha599-fbeceb59df.json";
    private static final String PROJECT_ID = "radinn-rindus";

    private static final String PATH_JETBODIES = "/jetbodies/{jbId}";
    private static final String TEST_PATH = "/test/{test}/pest/{pest}/rest";
    private static final String BEST_PATH = "/test/{test}/pest/{pest}/rest/{rest}/best/";

    // Register our path with handlers
    static {
        Routler.register(PATH_JETBODIES, new StationHandler());
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        initFirestore();

        String json = Routler.handle(request.getMethod(), request.getPath(), request, response);

        response.setContentType(CONTENT_TYPE);
        OutputStream os = response.getOutputStream();   // Must use OutputStream for UTF-8

        if (json != null) {
            response.setStatusCode(HttpURLConnection.HTTP_OK);
            os.write(bytes(json));
        } else {
            response.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
            os.write(bytes("{\"message\" : \"Something blew up!\"}"));
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

    public static Firestore getFirestore() {
        return firestore;
    }

    private static byte[] bytes(String str) {
        return str != null ? str.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8);
    }
}