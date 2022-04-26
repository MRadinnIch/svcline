package com.svcline.handlers.db;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.svcline.models.LineItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DbLineFacacde {
    private static final String COLLECTION_LIVE = "lineItem";
    private static final String COLLECTION_TEST = "lineItemTest";

    private static String activeCollection;
    private static Firestore db;

    public DbLineFacacde(Firestore firestore, Boolean liveEnvironment) {
        db = firestore;
        activeCollection = liveEnvironment ? COLLECTION_LIVE : COLLECTION_TEST;
    }

    public void set(LineItem lineItem) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> wr = db.collection(activeCollection).document(lineItem.getId()).set(lineItem);

        //noinspection StatementWithEmptyBody
        while (!wr.isDone()) { /* We lock the thread waiting for the Firestore operation to be done. using callbacks does not help  */ }

        // We get the result. If this throws an exception the "set" failed.
        //noinspection ResultOfMethodCallIgnored
        wr.get().getUpdateTime();
    }

    public void deleteFor(String itemId) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> wr = db.collection(activeCollection).document(itemId).delete();

        //noinspection StatementWithEmptyBody
        while (!wr.isDone()) { /* We lock the thread waiting for the Firestore operation to be done. using callbacks does not help  */ }

        //noinspection ResultOfMethodCallIgnored
        wr.get().getUpdateTime();
    }

    public LineItem getFor(String itemId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> query = db.collection(activeCollection).document(itemId).get();
        DocumentSnapshot document = query.get();

        return document.exists() ? document.toObject(LineItem.class) : null;
    }

    public ArrayList<LineItem> getAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = db.collection(activeCollection).get();

        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        ArrayList<LineItem> lineItems = new ArrayList<>();

        if (documents.isEmpty())
            return null;

        for (QueryDocumentSnapshot document : documents) {
            lineItems.add(document.toObject(LineItem.class));
        }

        return lineItems;
    }
}