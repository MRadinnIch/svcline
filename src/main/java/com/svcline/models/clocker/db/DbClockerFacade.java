package com.svcline.models.clocker.db;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.svcline.models.clocker.Clocker;
import com.svcline.models.clocker.Times;
import com.svcline.models.clocker.bq.BigQueryService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class DbClockerFacade {
    private static final Logger logger = Logger.getLogger(DbClockerFacade.class.getName());
    private static final String COLLECTION_LIVE = "clocker";
    private static final String COLLECTION_TEST = "clockerTest";

    private static String activeCollection;
    private static Firestore db;

    public DbClockerFacade(Firestore firestore, Boolean liveEnvironment) {
        db = firestore;
        activeCollection = liveEnvironment ? COLLECTION_LIVE : COLLECTION_TEST;
    }

    public void set(Clocker clocker) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> wr = db.collection(activeCollection).document(clocker.getItemId()).set(clocker);

        //noinspection StatementWithEmptyBody
        while (!wr.isDone()) { /* We lock the thread waiting for the Firestore operation to be done. using callbacks does not help  */ }

        // We get the result. If this throws an exception the "set" failed.
        //noinspection ResultOfMethodCallIgnored
        wr.get().getUpdateTime();
    }

    public ArrayList<Times> getTimesFor(String itemId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> query = db.collection(activeCollection).document(itemId).get(FieldMask.of("timesList"));

        DocumentSnapshot document = query.get();

        if (document.exists()) {
            logger.info("Clocker document: " + document);

            @SuppressWarnings("unchecked")
            ArrayList<Times> timesList = (ArrayList<Times>) document.get("timesList");
            return timesList;
        }

        return null;
    }

    public Clocker getFor(String itemId) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> query = db.collection(activeCollection).document(itemId).get();
        DocumentSnapshot document = query.get();

        return document.exists() ? document.toObject(Clocker.class) : null;
    }

    public ArrayList<Clocker> getAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = db.collection(activeCollection).get();

        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        ArrayList<Clocker> clockedItems = new ArrayList<>();

        if (documents.isEmpty())
            return null;

        for (QueryDocumentSnapshot document : documents) {
            clockedItems.add(document.toObject(Clocker.class));
        }

        return clockedItems;
    }


    public void deleteFor(String itemId) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> wr = db.collection(activeCollection).document(itemId).delete();

        //noinspection StatementWithEmptyBody
        while (!wr.isDone()) { /* We lock the thread waiting for the Firestore operation to be done. using callbacks does not help  */ }

        //noinspection ResultOfMethodCallIgnored
        wr.get().getUpdateTime();
    }
}
