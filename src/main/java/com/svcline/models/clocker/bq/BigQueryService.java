package com.svcline.models.clocker.bq;

import com.google.api.services.bigquery.model.TableCell;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;
import com.google.common.collect.ImmutableList;
import com.svcline.models.clocker.Clocker;
import com.svcline.models.clocker.Times;
import com.svcline.models.clocker.Timing;
import com.svcline.prodline.ProductionLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BigQueryService {
    private static final Logger logger = Logger.getLogger(BigQueryService.class.getName());
    private static final String ITEM_ID = "itemId";
    private static final String ITEM_PRODUCTION_TIME = "itemProductionTime";
    private static final String ESTIMATED_ITEM_PRODUCTION_TIME = "estimatedItemProductionTime";
    private static final String ITEM_PRODUCTION_TIME_DEVIATION = "itemProductionTimeDeviation";
    private static final String TIMES_LIST = "timesList";
    private static final String OPERATION = "operation";
    private static final String TIMESTAMP = "timestamp";
    private static final String STATION_ID = "stationId";
    private static final String STATION_PRODUCTION_TIME = "stationProductionTime";
    private static final String STATION_PRODUCTION_LEAD_TIME = "stationProductionLeadTime";
    private static final String ESTIMATED_STATION_PRODUCTION_TIME = "estimatedStationProductionTime";
    private static final String STATION_PRODUCTION_TIME_DEVIATION = "stationProductionTimeDeviation";
    private static final String TIMING_LIST = "timingList";

    private static final String TABLE_LIVE = "timekeeper";
    private static final String TABLE_TEST = "timekeeperTest";
    private static final String SERVICE_ACCOUNT = "radinn-rindus-sandbox-27eca0521d8d.json";
    private static final String PROJECT_ID_SANDBOX = "radinn-rindus-sandbox";
    private static final String PROJECT_ID_LIVE = "radinn-rindus-sandbox";
    private static final String datasetName = "production";

    private final String tableName;
    private final ProductionLine productionLine;
    private BigQuery bigquery = null;

    public BigQueryService(ProductionLine productionLine) {
        if (productionLine == null)
            throw new NullPointerException("Production line cannot be null");

        this.productionLine = productionLine;

        String projectId;
        String serviceAccount1;
        if (this.productionLine.getProps().isLiveEnv()) {
            this.tableName = TABLE_LIVE;
            projectId = PROJECT_ID_LIVE;
            serviceAccount1 = SERVICE_ACCOUNT;
        } else {
            this.tableName = TABLE_TEST;
            projectId = PROJECT_ID_SANDBOX;
            serviceAccount1 = SERVICE_ACCOUNT;
        }


        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(serviceAccount1);

        assert serviceAccount != null;
        try {
            bigquery = BigQueryOptions.newBuilder().setProjectId(projectId).setCredentials(GoogleCredentials.fromStream(serviceAccount)).build().getService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertEntry(Clocker clocker) {
        if (!this.productionLine.getProps().isTimekeeping()) {
            logger.info("Timekeeping not enabled. Times will not be measured.");
            return;
        } else if (!this.productionLine.getProps().isClocking()) {
            logger.info("Cannot measure times if clocking is disabled.");
        }

        BqEntry bqEntry = BqEntry.from(clocker);
        bqEntry.setEstimatedItemProductionTime(productionLine.getProductLineConfiguration().getEstimatedItemProductionTime());
        bqEntry.calculateItemProductionTime();
        bqEntry.calculateStationTimes();

        List<TableCell> timesListObject = new ArrayList<>();
        int ix = 0;
        for (Times times : bqEntry.getTimesList()) {
            List<TableCell> timingListObject = new ArrayList<>();
            for (Timing timing : times.getTimingList()) {
                timingListObject.add(new TableCell().set(OPERATION, timing.getOperation().name()).set(TIMESTAMP, timing.getTimestamp().toString()));
            }

            timesListObject.add(new TableCell()
                                        .set(STATION_ID, times.getStationId())
                                        .set(STATION_PRODUCTION_TIME, bqEntry.getStationProductionTimeFor(ix))
                                        .set(STATION_PRODUCTION_LEAD_TIME, bqEntry.getStationProductionLeadTimeFor(ix))
                                        .set(ESTIMATED_STATION_PRODUCTION_TIME, bqEntry.getEstimatedStationProductionTimeFor(ix))
                                        .set(STATION_PRODUCTION_TIME_DEVIATION, bqEntry.getStationProductionTimeDeviationFor(ix))
                                        .set(TIMING_LIST, timingListObject));

            ix++;
        }

        System.out.println("Item production lasted: " + bqEntry.getItemProductionTimeInSeconds());

        Map<String, Object> rowContent = new HashMap<>();
        rowContent.put(ITEM_ID, bqEntry.getItemId());
        rowContent.put(TIMES_LIST, timesListObject);
        rowContent.put(ITEM_PRODUCTION_TIME, bqEntry.getItemProductionTimeInSeconds());
        rowContent.put(ESTIMATED_ITEM_PRODUCTION_TIME, bqEntry.getEstimatedItemProductionTime());
        rowContent.put(ITEM_PRODUCTION_TIME_DEVIATION, bqEntry.getItemProductionTimeDeviation());

        InsertAllResponse response =
                bigquery.insertAll(
                        InsertAllRequest.newBuilder(TableId.of(datasetName, tableName))
                                .setRows(ImmutableList.of(InsertAllRequest.RowToInsert.of(rowContent)))
                                .build());

        if (response.hasErrors()) {
            // If any of the insertions failed, this lets you inspect the errors
            for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                logger.info("Response error: \n" + entry.getValue());
            }
        }
    }
}
