package com.svcline.models;

import com.google.cloud.firestore.annotation.Exclude;
import com.svcline.prodline.Transition;

public class LineItem extends Transition {
    private String previousStationId;

    public LineItem() {
    }

    public LineItem(Transition tr) {
        this.id = tr.getId();
        this.currentStationId = tr.getCurrentStationId();
        this.previousStationId = null;
        this.state = tr.getState();
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void clearPreviousStation() {
        this.previousStationId = null;
    }

    public void clearCurrentStation() {
        this.previousStationId = null;
    }

    public boolean validate() {
        return !(this.id == null || this.currentStationId == null);
    }

    @Exclude
    public boolean isScrapped() {
        return this.state == State.SCRAPED;
    }

    @Exclude
    public boolean isDone() {
        return this.state == State.FINISHED;
    }

    @Exclude
    public boolean isPassed(){
        return this.state == State.PASSED;
    }

    @Exclude
    public boolean isFailed() {
        return this.state == State.FAILED;
    }

    @Exclude
    public boolean isStarted() {
        return this.state == State.STARTED;
    }

    @Exclude
    public boolean isRetried() {
        return this.state == State.RETRIED;
    }

    @Exclude
    public boolean isCreated() {
        return this.state == State.CREATED;
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
