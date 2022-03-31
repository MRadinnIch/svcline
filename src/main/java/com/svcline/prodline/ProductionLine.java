package com.svcline.prodline;

import com.svcline.models.LineItem;
import com.svcline.models.State;
import com.svcline.models.Station;
import com.svcline.models.StationType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ProductionLine {
    private String startStationId;
    private String endStationId;
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

    public boolean init(HashMap<String, Station> productionLine, HashMap<String, String> stationOrder) throws InstantiationException {
        // The production line must be set and the start and service stations must exist
        //if (productionLine != null && !productionLine.isEmpty() && productionLine.containsKey(startStationId) && productionLine.containsKey(serviceStationId)) {
        if (productionLine != null && !productionLine.isEmpty() && !hasDuplicateStations()) {
            // Check if stations are correctly defined, meaning START, END and SERVICE
            int start = 0;
            int end = 0;
            int service = 0;
            for (Station station : productionLine.values()) {
                if (station.getStationType() == StationType.START) {
                    this.startStationId = station.getId();
                    start++;
                } else if (station.getStationType() == StationType.END) {
                    this.endStationId = station.getId();
                    end++;
                } else if (station.getStationType() == StationType.SERVICE) {
                    this.serviceStationId = station.getId();
                    service++;
                }
            }

            // We must have three stations
            if (start == 0 || end == 0 || service == 0)
                throw new InstantiationException("Initialization failed, production line must one START, STOP and SERVICE station.");

            this.stationMap = productionLine;
            this.stationOrder = stationOrder;
            this.initialized = true;

            return true;
        }

        return false;
    }

    private boolean hasDuplicateStations() {
        // @todo: Implement method
        return false;
    }

    public String getStartStationId() {
        return startStationId;
    }

    public void setStartStationId(String startStationId) {
        this.startStationId = startStationId;
    }

    public String getEndStationId() {
        return endStationId;
    }

    public void setEndStationId(String endStationId) {
        this.endStationId = endStationId;
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

    public LineItem startProduction(String id) {
        return new LineItem(id, startStationId, null, State.START);
    }

    public LineItem toNextStation(@NotNull LineItem actualItem, @NotNull LineItem currentLineItem) throws IllegalStateException {
        State newState = currentLineItem.getState();
        Station currentStation = getStation(currentLineItem.getCurrentStationId());

        if (!isInCorrectLineOrder(actualItem, currentLineItem)) {
            throw new IllegalStateException("Item is not in correct production line station.");
        } else if (actualItem.isScrapped()) {
            throw new IllegalStateException("Scrapped items cannot be processed");
        } else if (actualItem.isDone()) {
            throw new IllegalStateException("Finished items cannot be processed");
        } else if (!currentStation.allowedState(newState)) {
            throw new IllegalStateException("State '" + newState + "' not allowed for station. Allowed states are: " + currentStation.getAllowedStates());
        } else if (actualItem.isFailed() && !currentStation.isServiceStation()) {
            throw new IllegalStateException("This item should in the service station. This station is a '" + currentStation.getStationType() + "'.");
        }

        //State currentState = currentLineItem.getState();
        LineItem lineItem = new LineItem(currentLineItem);
        switch (newState) {
            case RETRY:
                lineItem.setPreviousStationId(actualItem.getCurrentStationId());
                lineItem.setCurrentStationId(actualItem.getPreviousStationId());
                lineItem.setState(newState);
                break;

            case PASS:
                if (currentStation.isEndStation()) {
                    lineItem.setState(State.DONE);
                    lineItem.setCurrentStationId(this.endStationId);
                } else {
                    lineItem.setState(newState);
                    lineItem.setCurrentStationId(getNextStationId(lineItem.getPreviousStationId()));
                }

                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());

                break;

            case FAIL:
                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());
                lineItem.setCurrentStationId(this.serviceStationId);    // If we fail we go to the service station
                lineItem.setState(newState);
                break;

            case SCRAP:
                lineItem.setCurrentStationId(getServiceStationId());
                lineItem.setPreviousStationId(getServiceStationId());
                lineItem.setState(newState);
                break;

            case DONE:
                lineItem.setCurrentStationId(this.endStationId);
                lineItem.setPreviousStationId(currentLineItem.getCurrentStationId());
                lineItem.setState(newState);

            default:
                // Other states not handled
        }

        return lineItem;
    }

    private boolean isInCorrectLineOrder(LineItem actualLineItem, LineItem currentItem) {
        if (actualLineItem == null || currentItem == null || actualLineItem.getId() == null || currentItem.getCurrentStationId() == null)
            return false;
        else if (actualLineItem.isFailed())
            return true;

        // We do not check failed items.
        return currentItem.getCurrentStationId().equals(this.stationOrder.get(actualLineItem.getCurrentStationId()));
    }
}
