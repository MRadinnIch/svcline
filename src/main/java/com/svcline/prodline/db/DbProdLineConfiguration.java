package com.svcline.prodline.db;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.svcline.prodline.ProductLineConfiguration;

import java.util.concurrent.ExecutionException;

public class DbProdLineConfiguration {
    private static final String COLLECTION = "productLineConfiguration";
    private static final String ITEM_ID = "demo-configuration";

    private Firestore db;

    public DbProdLineConfiguration(Firestore firestore) {
        db = firestore;
    }

    public void write(String configId, ProductLineConfiguration configuration) {
        db.collection(COLLECTION).document(configId).set(configuration);
    }

    public ProductLineConfiguration read(String id) {
        ApiFuture<DocumentSnapshot> query = db.collection(COLLECTION).document(id).get();
        try {
            DocumentSnapshot document = query.get();
            ProductLineConfiguration plc = document.toObject(ProductLineConfiguration.class);
            return plc;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}
