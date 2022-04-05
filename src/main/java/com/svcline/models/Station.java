package com.svcline.models;

import com.google.cloud.firestore.annotation.Exclude;

import java.util.ArrayList;

public class Station {
    private String id;
    private String name;
    private StationType stationType;
    //private ArrayList<State> allowedStates;
    private ArrayList<Action> allowedActions;

    public Station() {
    }

    /*public Station(String id, String name, StationType stationType, ArrayList<State> allowedStates) {
        this.id = id;
        this.name = name;
        this.stationType = stationType;
        this.allowedStates = allowedStates;
    }*/

    public Station(String id, String name, StationType stationType, ArrayList<Action> allowedActions) {
        this.id = id;
        this.name = name;
        this.stationType = stationType;
        this.allowedActions = allowedActions;
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

/*    public void setAllowedStates(ArrayList<State> allowedStates) {
        this.allowedStates = allowedStates;
    }*/

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

    /*public boolean allowedState(State stateToCompare) {
        for (State state : allowedStates) {
            if(state == stateToCompare)
                return true;
        }

        return false;
    }*/

    public boolean allowedState(State stateToCompare) {
        for (Action action : allowedActions) {
            if(action.getState() == stateToCompare)
                return true;
        }

        return false;
    }
}
