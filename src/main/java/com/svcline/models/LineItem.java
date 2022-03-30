package com.svcline.models;

public class LineItem {
    private String id;
    private String currentStationId;
    private String previousStationId;
    private State state;

    public LineItem() {
    }

    public LineItem(String id, String currentStationId) {
        this.id = id;
        this.currentStationId = currentStationId;
        this.previousStationId = null;
        this.state = State.START;
    }

    public LineItem(String id, String currentStationId, String previousStationId, State state) {
        this.id = id;
        this.currentStationId = currentStationId;
        this.previousStationId = previousStationId;
        this.state = state;
    }

    public LineItem(LineItem lineItem) {
        this.id = lineItem.id;
        this.currentStationId = lineItem.currentStationId;
        this.previousStationId = lineItem.previousStationId;
        this.state = lineItem.state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurrentStationId() {
        return currentStationId;
    }

    public void setCurrentStationId(String currentStationId) {
        this.currentStationId = currentStationId;
    }

    public String getPreviousStationId() {
        return previousStationId;
    }

    public void setPreviousStationId(String previousStationId) {
        this.previousStationId = previousStationId;
    }

    public void clearPreviousStation() {
        this.previousStationId = null;
    }

    public void clearCurrentStation() {
        this.previousStationId = null;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean validate() {
        return true;
            //return !(this.id == null || this.currentStationId == null || (this.previousStationId == null && this.state != State.START) ||
            //                                                                (this.previousStationId != null && this.state == State.START ));
    }

    @Override
    public String toString() {
        return "LineItem{" +
               "id='" + id + '\'' +
               ", currentStationId='" + currentStationId + '\'' +
               ", previousStationId='" + previousStationId + '\'' +
               ", state=" + state +
               '}';
    }
}
