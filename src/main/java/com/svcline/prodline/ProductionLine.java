package com.svcline.prodline;

import com.svcline.models.LineItem;
import com.svcline.models.State;
import com.svcline.models.Station;
import com.svcline.models.StationType;

import java.util.HashMap;
import java.util.Map;

public class ProductionLine {
    private String startStationId;
    private String endStationId;
    private String serviceStationId;
    private HashMap<String, Station> stationMap;
    private HashMap<String, String> stationTransitionMap;

    private boolean init = false;

    public ProductionLine() {
        this.stationMap = null;
        this.startStationId = null;
        this.serviceStationId = null;
        this.endStationId = null;
        this.stationTransitionMap = null;
    }

    private void initCheck() throws InstantiationException {
        if (!this.init)
            throw new InstantiationException("Production line has not been done. Aborting execution.");
    }

    private boolean stationOrderCorrect() {
        for (Map.Entry<String, String> entry : stationTransitionMap.entrySet()) {
            if (!stationMap.containsKey(entry.getKey()) || !stationMap.containsKey(entry.getValue()))
                return false;
        }

        return true;
    }

    public void init(StationMap stationMapInit, StationOrderMap stationOrderMapInit) throws InstantiationException {
        // We must have three stations
        if (stationMapInit == null || stationMapInit.getStationMap().isEmpty())
            throw new InstantiationException("Initialization failed: Station map cannot be empty. Terminating execution.");
        this.stationMap = stationMapInit.getStationMap();

        if (stationOrderMapInit == null || stationOrderMapInit.getStationOrder().isEmpty())
            throw new InstantiationException("Initialization failed. Station order map cannot be empty. Terminating execution.");
        this.stationTransitionMap = stationOrderMapInit.getStationOrder();

        // It's now safe to check if the station order map consist of existing stations. If not, we exit.
        if (!stationOrderCorrect())
            throw new InstantiationException("Initialization failed. Station order map contains non-existant stations. Terminating execution.");

        // Check if stations are correctly defined, meaning START, END and SERVICE
        int start = 0;
        int end = 0;
        int service = 0;
        for (Station station : this.stationMap.values()) {
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
            throw new InstantiationException("Initialization failed: production line must have one START, STOP and SERVICE station. Terminating execution.");

        this.init = true;
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

    public Station getStation(String stationId) {
        if (stationId == null || stationId.isEmpty() || stationId.isBlank()) {
            return null;
        }

        return this.stationMap.get(stationId);
    }

    private String getNextStationId(String currentStationId) {
        return this.stationTransitionMap.get(currentStationId);
    }

    public LineItem startProduction(String id) throws InstantiationException {
        initCheck();

        return new LineItem(id, startStationId, null, State.START);
    }

    public HashMap<String, Station> getStationMap() {
        return stationMap;
    }

    public void setStationMap(HashMap<String, Station> stationMap) {
        this.stationMap = stationMap;
    }

    public HashMap<String, String> getStationTransitionMap() {
        return stationTransitionMap;
    }

    public void setStationTransitionMap(HashMap<String, String> stationTransitionMap) {
        this.stationTransitionMap = stationTransitionMap;
    }

    public LineItem toNextStation(LineItem actualItem, LineItem currentLineItem) throws IllegalStateException, InstantiationException {
        initCheck();

        State newState = currentLineItem.getState();
        Station currentStation = getStation(currentLineItem.getCurrentStationId());

        if (!isInCorrectLineOrder(actualItem, currentLineItem)) {
            throw new IllegalStateException("This item is not at the correct production line station. Correct station is: " +
                                            getNextStationId(actualItem.currentStationId));
        } else if (actualItem.isScrapped()) {
            throw new IllegalStateException("Scrapped items cannot be processed");
        } else if (actualItem.isDone()) {
            throw new IllegalStateException("Finished items cannot be processed");
        } else if (!currentStation.allowedState(newState)) {
            throw new IllegalStateException("State '" + newState + "' not allowed for station. Allowed states are: " + currentStation.getAllowedStates());
        } else if (actualItem.isFailed() && !currentStation.isServiceStation()) {
            throw new IllegalStateException("This item should in the service station. This station is a '" + currentStation.getStationType() + "'.");
        }

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
                    //lineItem.setCurrentStationId(...); We do not set current station ID since the one passed in is correct
                }

                lineItem.setPreviousStationId(actualItem.getCurrentStationId());

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
            return true;    // We do not check failed items.

        return currentItem.getCurrentStationId().equals(this.stationTransitionMap.get(actualLineItem.getCurrentStationId()));
    }
}
