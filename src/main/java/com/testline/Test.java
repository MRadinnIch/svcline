package com.testline;

import com.svcline.LineService;
import com.svcline.models.*;
import com.svcline.prodline.*;
import com.routler.RResponse;

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

        Action pass = new Action("Next", State.PASS);
        Action failed = new Action("Failed", State.FAIL);
        Action retry = new Action("Retry", State.RETRY);
        Action scrap = new Action("Scrap item", State.SCRAP);

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

        Transition create = new Transition(itemId1);
        Transition second = new Transition(itemId1, station2.getId(), State.PASS);
        Transition third = new Transition(itemId1, station3.getId(), State.PASS);
        Transition fourth = new Transition(itemId1, station4.getId(), State.PASS);
        Transition end = new Transition(itemId1, station5.getId(), State.PASS);

        lineService.prepareItem(create);
        run(lineService.create(create), true);

        lineService.prepareItem(second);
        run(lineService.toNext(second), true);

        lineService.prepareItem(third);
        run(lineService.toNext(third), true);

        lineService.prepareItem(fourth);
        run(lineService.toNext(fourth), true);

        lineService.prepareItem(end);
        run(lineService.toNext(end), true);
    }

    private static void run(RResponse lr, boolean succeed) {
        System.out.println(lr);
        if (succeed)
            assert (lr.succeeded());
        else
            assert (!lr.succeeded());
    }
}