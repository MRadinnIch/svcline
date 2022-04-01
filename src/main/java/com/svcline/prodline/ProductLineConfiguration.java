package com.svcline.prodline;

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

    public void writeToDb(){
        DbProdLineConfiguration.write(this);
    }

    public static ProductLineConfiguration loadFromDb(){
        return DbProdLineConfiguration.read("demo-configuration");
    }

    public static ProductLineConfiguration loadFromDb(String id){
        return DbProdLineConfiguration.read(id);
    }

    public void loadTestConfiguration() {
        Station station1 = new Station("2001", "Start Station", StationType.START,
                                       new ArrayList<>(List.of(State.PASS, State.FAIL)));

        Station station2 = new Station("2002", "Second Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(State.PASS, State.FAIL)));

        Station station3 = new Station("2003", "Third Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(State.PASS, State.FAIL)));

        Station station4 = new Station("2004", "Fourth Station", StationType.PRODUCTION,
                                       new ArrayList<>(List.of(State.PASS, State.FAIL)));

        Station station5 = new Station("2005", "End Station", StationType.END,
                                       new ArrayList<>(List.of(State.PASS, State.FAIL)));

        Station serviceStation = new Station("1001", "Service Station", StationType.SERVICE,
                                             new ArrayList<>(List.of(State.PASS, State.RETRY, State.SCRAP)));
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
    }
}