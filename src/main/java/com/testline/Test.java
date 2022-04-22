package com.testline;

import com.routler.RResponse;
import com.svcline.LineService;
import com.svcline.models.*;
import com.svcline.prodline.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

        Action pass = new Action("Next", State.PASSED);
        Action failed = new Action("Failed", State.FAILED);
        Action retry = new Action("Retry", State.RETRIED);
        Action scrap = new Action("Scrap item", State.SCRAPED);

        Station station1 = new Station("3001", "Start Station", StationType.START,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station2 = new Station("3002", "Second Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station3 = new Station("3003", "Third Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station4 = new Station("3004", "Fourth Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station5 = new Station("3005", "End Station", StationType.END,
                                       new ArrayList<>(List.of(pass, failed)));

        Station serviceStation = new Station("6001", "Service Station", StationType.SERVICE,
                                             new ArrayList<>(List.of(pass, retry, scrap)));
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

        String itemId1 = "100001";
        LineService lineService = new LineService(productionLine);

        Transition create = new Transition(itemId1, station1.getId(), State.PASSED);
        Transition second = new Transition(itemId1, station2.getId(), State.PASSED);
        Transition third = new Transition(itemId1, station3.getId(), State.PASSED);
        Transition fourth = new Transition(itemId1, station4.getId(), State.PASSED);
        Transition end = new Transition(itemId1, station5.getId(), State.PASSED);

        //lineService.stationItemStart(create);
        run(lineService.startProduction(new Transition(create.getId(), create.getCurrentStationId(), State.CREATED)), true);
        run(lineService.stationItemStop(create), true);

        run(lineService.stationItemStart(second), true);
        run(lineService.stationItemStop(second), true);

        run(lineService.stationItemStart(third), true);
        run(lineService.stationItemStop(third), true);

        run(lineService.stationItemStart(fourth), true);
        run(lineService.stationItemStop(fourth), true);

        run(lineService.stationItemStart(end), true);
        run(lineService.stationItemStop(end), true);
    }

    @SuppressWarnings("SameParameterValue")
    private static void run(RResponse lr, boolean succeed) {
        System.out.println(lr);
        if (succeed)
            assert (lr.succeeded());
        else
            assert (!lr.succeeded());
    }
}