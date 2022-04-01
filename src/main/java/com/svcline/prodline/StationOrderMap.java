package com.svcline.prodline;

import java.util.HashMap;

public class StationOrderMap {
    private final HashMap<String, String> stationOrder;

    public StationOrderMap() {
        stationOrder = new HashMap<>();
    }

    public void addStationTransition(String fromStationId, String toStationId) {
        stationOrder.putIfAbsent(fromStationId, toStationId);
    }

    public HashMap<String, String> getStationOrder() {
        return stationOrder;
    }
}
