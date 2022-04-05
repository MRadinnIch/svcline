package com.svcline.prodline.db;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.internal.NonNull;
import com.svcline.prodline.ProductLineConfiguration;
import com.svcline.svcline;

import java.util.concurrent.ExecutionException;

public class DbProdLineConfiguration {
    private static final String COLLECTION = "productLineConfiguration";
    private static final String ITEM_ID = "demo-configuration";

    public static void write(String configId, ProductLineConfiguration configuration) {
        Firestore db = svcline.getFirestore();

        db.collection(COLLECTION).document(configId).set(configuration);
    }

    public static ProductLineConfiguration read(String id) {
        Firestore db = svcline.getFirestore();

        ApiFuture<DocumentSnapshot> query = db.collection(COLLECTION).document(id).get();
        try {
            DocumentSnapshot document = query.get();
            return document.toObject(ProductLineConfiguration.class);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
