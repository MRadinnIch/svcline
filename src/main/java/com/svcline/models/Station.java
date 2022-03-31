package com.svcline.models;

import java.util.ArrayList;

public class Station {
    private String id;
    private String name;
    private StationType stationType;
    private ArrayList<State> allowedStates;

    public Station(String id, String name, StationType stationType, ArrayList<State> allowedStates) {
        this.id = id;
        this.name = name;
        this.stationType = stationType;
        this.allowedStates = allowedStates;
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

    public ArrayList<State> getAllowedStates() {
        return allowedStates;
    }

    public void setAllowedStates(ArrayList<State> allowedStates) {
        this.allowedStates = allowedStates;
    }

    public boolean isServiceStation() {
        return this.stationType == StationType.SERVICE;
    }

    public boolean isEndStation() {
        return this.stationType == StationType.END;
    }

    public boolean isStartStation() {
        return this.stationType == StationType.START;
    }

    public boolean allowedState(State stateToCompare) {
        for (State state : allowedStates) {
            if(state == stateToCompare)
                return true;
        }

        return false;
    }
}
