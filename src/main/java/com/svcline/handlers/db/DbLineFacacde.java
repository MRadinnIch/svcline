package com.svcline.handlers.db;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.internal.NonNull;
import com.svcline.models.LineItem;
import com.svcline.svcline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DbLineFacacde {
    private static final String COLLECTION = "lineItem";

    public static void set(LineItem lineItem) {
        Firestore db = svcline.getFirestore();

        db.collection(COLLECTION).document(lineItem.getId()).set(lineItem);
    }

    public static LineItem getFor(@NonNull String itemId) throws ExecutionException, InterruptedException {
        Firestore db = svcline.getFirestore();

        ApiFuture<DocumentSnapshot> query = db.collection(COLLECTION).document(itemId).get();
        DocumentSnapshot document = query.get();

        return document.exists() ? document.toObject(LineItem.class) : null;
    }

    public static ArrayList<LineItem> getAll() throws ExecutionException, InterruptedException {
        Firestore db = svcline.getFirestore();

        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION).get();

        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        ArrayList<LineItem> lineItems = new ArrayList<>();

        if(documents.isEmpty())
            return null;

        for (QueryDocumentSnapshot document : documents) {
            lineItems.add(document.toObject(LineItem.class));
        }

        return lineItems;
    }
}
