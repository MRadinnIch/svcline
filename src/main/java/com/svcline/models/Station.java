package com.svcline.models;

import com.google.cloud.firestore.annotation.Exclude;

import java.util.ArrayList;

public class Station {
    private String id;
    private String name;
    private StationType stationType;
    private ArrayList<Action> allowedActions;
    private Double estimatedStationProductionTime;

    public Station() {
    }

    public Station(String id, String name, StationType stationType, ArrayList<Action> allowedActions, Double estimatedStationProductionTime) {
        this.id = id;
        this.name = name;
        this.stationType = stationType;
        this.allowedActions = allowedActions;
        this.estimatedStationProductionTime = estimatedStationProductionTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StationType getStationType() {
        return stationType;
    }

    public void setStationType(StationType stationType) {
        this.stationType = stationType;
    }

    @Exclude
    public ArrayList<State> getAllowedStates() {
        ArrayList<State> allowedStates = new ArrayList<>();

        for (Action action : allowedActions) {
            allowedStates.add(action.getState());
        }

        return allowedStates;
    }

    public ArrayList<Action> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(ArrayList<Action> allowedActions) {
        this.allowedActions = allowedActions;
    }

    @Exclude
    public boolean isServiceStation() {
        return this.stationType == StationType.SERVICE;
    }

    @Exclude
    public boolean isEndStation() {
        return this.stationType == StationType.END;
    }

    @Exclude
    public boolean isStartStation() {
        return this.stationType == StationType.START;
    }

    public Double getEstimatedStationProductionTime() {
        return estimatedStationProductionTime;
    }

    public void setEstimatedStationProductionTime(Double estimatedStationProductionTime) {
        this.estimatedStationProductionTime = estimatedStationProductionTime;
    }

    public boolean allowedState(State stateToCompare) {
        for (Action action : allowedActions) {
            if(action.getState() == stateToCompare)
                return true;
        }

        return false;
    }
}
