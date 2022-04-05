package com.svcline.prodline;

import com.svcline.models.Action;
import com.svcline.models.State;
import com.svcline.models.Station;
import com.svcline.models.StationType;
import com.svcline.prodline.db.DbProdLineConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ProductLineConfiguration {
    StationMap configuredStationMap;
    StationOrderMap configuredStationOrder;

    public ProductLineConfiguration() {
        configuredStationMap = new StationMap();
        configuredStationOrder = new StationOrderMap();
    }

    public StationMap getConfiguredStationMap() {
        return configuredStationMap;
    }

    public void setConfiguredStationMap(StationMap configuredStationMap) {
        this.configuredStationMap = configuredStationMap;
    }

    public StationOrderMap getConfiguredStationOrder() {
        return configuredStationOrder;
    }

    public void setConfiguredStationOrder(StationOrderMap configuredStationOrder) {
        this.configuredStationOrder = configuredStationOrder;
    }

    public void writeToDb(String configId){
        DbProdLineConfiguration.write(configId,this);
    }

    public static ProductLineConfiguration loadFromDb(String configId){
        return DbProdLineConfiguration.read(configId);
    }

    public void loadTestConfiguration() {
        Action pass = new Action("Next", State.PASS);
        Action failed = new Action("Failed", State.FAIL);
        Action retry = new Action("Retry", State.RETRY);
        Action scrap = new Action("Scrap item", State.SCRAP);

        Station station1 = new Station("1001", "Start Station", StationType.START,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station2 = new Station("1002", "Second Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station3 = new Station("1003", "Third Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(pass, failed)));

        Station station4 = new Station("1004", "End Station", StationType.END,
                                       new ArrayList<>(List.of(pass, failed)));

        //Station station5 = new Station("2005", "End Station", StationType.END,
        //                               new ArrayList<>(List.of(State.PASS, State.FAIL)));

        Station serviceStation = new Station("2001", "Service Station", StationType.SERVICE,
                                             new ArrayList<>(List.of(pass, retry, scrap)));
        configuredStationMap.addStation(station1);
        configuredStationMap.addStation(station2);
        configuredStationMap.addStation(station3);
        configuredStationMap.addStation(station4);
        configuredStationMap.addStation(serviceStation);

        configuredStationOrder.addStationTransition(station1.getId(), station2.getId());
        configuredStationOrder.addStationTransition(station2.getId(), station3.getId());
        configuredStationOrder.addStationTransition(station3.getId(), station4.getId());
    }
}