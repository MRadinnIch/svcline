package com.svcline.prodline;

import com.svcline.models.Station;

import java.util.HashMap;

public class StationMap {
    private final HashMap<String, Station> stationMap;

    public StationMap() {
        stationMap = new HashMap<>();
    }

    public void addStation(Station station) {
        stationMap.putIfAbsent(station.getId(), station);
    }

    public HashMap<String, Station> getStationMap() {
        return stationMap;
    }

    public boolean registered(String k) {
        return stationMap.containsKey(k);
    }
}
