package com.testline;

import com.routler.RResponse;
import com.svcline.LineService;
import com.svcline.models.*;
import com.svcline.prodline.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Test {
    private static final int TIMEOUT = 1;

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Props props;
        try {
            props = new Props();
            props.setEnvironment("test");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load properties. Aborting execution.");
            return;
        }

        ///////////////// Create the test production line configuration /////////////////
        StationMap configuredStationMap = new StationMap();
        StationOrderMap configuredStationOrder = new StationOrderMap();
        ProductLineConfiguration productLineConfiguration = new ProductLineConfiguration();
        productLineConfiguration.setVersion("1");
        productLineConfiguration.setConfiguredStationMap(configuredStationMap);
        productLineConfiguration.setConfiguredStationOrder(configuredStationOrder);
        productLineConfiguration.setEstimatedItemProductionTime(15.0);

        Action pass = new Action("Next", State.PASSED);
        Action failed = new Action("Failed", State.FAILED);
        Action retry = new Action("Retry", State.RETRIED);
        Action scrap = new Action("Scrap item", State.SCRAPED);
        Action created = new Action("Created item", State.CREATED);

        Station station1 = new Station("3001", "Start Station", StationType.START,
                                       new ArrayList<>(List.of(pass, failed, created)), 1.0);

        Station station2 = new Station("3002", "Second Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)), 2.0);

        Station station3 = new Station("3003", "Third Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)), 3.0);

        Station station4 = new Station("3004", "Fourth Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)), 4.0);

        Station station5 = new Station("3005", "End Station", StationType.END,
                                       new ArrayList<>(List.of(pass, failed)), 5.0);

        Station serviceStation = new Station("6001", "Service Station", StationType.SERVICE,
                                             new ArrayList<>(List.of(pass, retry, scrap)), 6.0);
        configuredStationMap.addStation(station1);
        configuredStationMap.addStation(station2);
        configuredStationMap.addStation(station3);
        configuredStationMap.addStation(station4);
        configuredStationMap.addStation(station5);
        configuredStationMap.addStation(serviceStation);

        configuredStationOrder.addStationTransition(station1.getId(), station2.getId());
        configuredStationOrder.addStationTransition(station2.getId(), station3.getId());
        configuredStationOrder.addStationTransition(station3.getId(), station4.getId());
        configuredStationOrder.addStationTransition(station4.getId(), station5.getId());

        ProductionLine productionLine;
        try {
            productionLine = new ProductionLine();
            productionLine.init(productLineConfiguration, props);
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
            System.out.println("Failed to init production line. Aborting execution.");
            return;
        }
        ///////////////// End product line configuration /////////////////

        LineService lineService = new LineService(productionLine);

        ArrayList<String> itemIdList = new ArrayList<>(List.of("100001", "100002", "100003", "100004", "100005"));
        for (String itemId : itemIdList) {
            Transition create = new Transition(itemId, station1.getId(), State.PASSED);
            Transition second = new Transition(itemId, station2.getId(), State.PASSED);
            Transition third = new Transition(itemId, station3.getId(), State.PASSED);
            Transition fourth = new Transition(itemId, station4.getId(), State.PASSED);
            Transition end = new Transition(itemId, station5.getId(), State.PASSED);

            run(lineService.startProduction(create), true);
            run(lineService.stationItemStop(create), true);

            run(lineService.stationItemStart(second), true);
            run(lineService.stationItemStop(second), true);

            run(lineService.stationItemStart(third), true);
            run(lineService.stationItemStop(third), true);

            run(lineService.stationItemStart(fourth), true);
            run(lineService.stationItemStop(fourth), true);

            run(lineService.stationItemStart(end), true);
            run(lineService.stationItemStop(end), true);

            //run(lineService.deleteLineEntryForItem(itemId), true);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void run(RResponse lr, boolean succeed) {
        int low = 100;
        int high = 2500;
        int sleep = new Random().nextInt(high - low) + low;

        try {
            TimeUnit.MILLISECONDS.sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(lr);
        if (succeed)
            assert (lr.succeeded());
        else
            assert (!lr.succeeded());
    }
}