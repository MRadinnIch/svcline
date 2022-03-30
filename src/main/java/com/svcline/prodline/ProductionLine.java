package com.svcline.prodline;

import com.svcline.models.LineItem;
import com.svcline.models.State;
import com.svcline.models.Station;

import java.util.HashMap;

public class ProductionLine {
    private String startStationId;
    private String serviceStationId;
    private HashMap<String, Station> stationMap;
    private HashMap<String, String> stationOrder;
    private boolean initialized;

    public ProductionLine() {
        this.stationMap = null;
        this.startStationId = null;
        this.serviceStationId = null;
        this.stationOrder = null;
        this.initialized = false;
    }

    public boolean init(String startStationId, String serviceStationId, HashMap<String, Station> productionLine, HashMap<String, String> stationOrder) {
        // The production line must be set and the start and service stations must exist
        if (productionLine != null && !productionLine.isEmpty() && productionLine.containsKey(startStationId) && productionLine.containsKey(serviceStationId)) {
            this.stationMap = productionLine;
            this.startStationId = startStationId;
            this.serviceStationId = serviceStationId;
            this.stationOrder = stationOrder;
            this.initialized = true;

            return true;
        }

        return false;
    }

    public String getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(String startStationId) {
        this.startStationId = startStationId;
    }

    public String getServiceStationId() {
        return serviceStationId;
    }

    public void setServiceStationId(String servicetStationId) {
        this.serviceStationId = servicetStationId;
    }

    public boolean addStation(Station station) {
        if (station != null && !station.getId().isBlank() && !station.getId().isEmpty()) {
            if (this.stationMap == null) {
                this.stationMap = new HashMap<>();
            }

            // We only add of not existing
            if (!this.stationMap.containsKey(station.getId())) {
                this.stationMap.put(station.getId(), station);
                return true;
            }
        }

        return false;
    }

    public Station getStation(String stationId) {
        if (stationId == null || stationId.isEmpty() || stationId.isBlank()) {
            return null;
        }

        return this.stationMap.get(stationId);
    }

    private String getNextStationId(String currentStationId) {
        return this.stationOrder.get(currentStationId);
    }

    public LineItem handleLineItem(LineItem currentLineItem, String stationId, State newState) {
        Station station = getStation(stationId);

        //State currentState = currentLineItem.getState();
        LineItem lineItem = new LineItem(currentLineItem);
        switch (newState) {
            case START:
                lineItem.clearPreviousStation();
                lineItem.setCurrentStationId(this.startStationId);
                lineItem.setState(newState);
                break;

            case PASS:
                lineItem.setCurrentStationId(getNextStationId(lineItem.getPreviousStationId()));
                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());
                lineItem.setState(newState);
                break;

            case FAIL:
                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());
                lineItem.setCurrentStationId(this.serviceStationId);    // If we fail we go to the service station
                lineItem.setState(newState);
                break;

            case SCRAP:
                lineItem.clearPreviousStation();
                lineItem.clearCurrentStation();
                lineItem.setState(newState);
                break;

            default:
                // Should never happen
        }

        return lineItem;
    }

    public boolean isInCorrectLineOrder(LineItem actualLineItem, String nextStationId) {
        if (actualLineItem == null || actualLineItem.getId() == null || nextStationId == null) {
            return false;
        }

        System.out.println("nextStationId :" + nextStationId);
        System.out.println("actualLineItem.getCurrentStationId(): " + actualLineItem.getCurrentStationId());
        System.out.println("stationOrder: " + this.stationOrder.get(actualLineItem.getCurrentStationId()));
        return nextStationId.equals(this.stationOrder.get(actualLineItem.getCurrentStationId()));
    }
}
