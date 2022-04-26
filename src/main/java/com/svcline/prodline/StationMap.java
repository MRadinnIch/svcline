package com.svcline.prodline;

import com.svcline.models.Station;

import java.util.HashMap;

public class StationMap {
    private HashMap<String, Station> stationMap;

    public StationMap() {
        stationMap = new HashMap<>();
    }

    public void addStation(Station station) {
        stationMap.putIfAbsent(station.getId(), station);
    }

    public HashMap<String, Station> getStationMap() {
        return stationMap;
    }

    public void setStationMap(HashMap<String, Station> stationMap) {
        this.stationMap = stationMap;
    }

    public Station getStationById(String stationId) {
        return stationMap.get(stationId);
    }

    public boolean registered(String k) {
        return stationMap.containsKey(k);
    }

    @Override
    public String toString() {
        return "StationMap{" +
               "stationMap=" + stationMap +
               '}';
    }
}
