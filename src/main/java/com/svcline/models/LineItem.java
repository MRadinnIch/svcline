package com.svcline.models;

import com.google.cloud.firestore.annotation.Exclude;
import com.google.cloud.functions.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

public class LineItem {
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private String id;
    private String currentStationId;
    private String previousStationId;
    private State state;

    public LineItem() {
    }

    public LineItem(HttpRequest request) throws IOException {
        LineItem li = gson.fromJson(request.getReader(), LineItem.class);

        this.id = li.id;
        this.currentStationId = li.currentStationId;
        this.previousStationId = li.previousStationId;
        this.state = li.state;
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
        return (this.state == State.SCRAP);
    }

    @Exclude
    public boolean isDone() {
        return (this.state == State.DONE);
    }

    @Exclude
    public boolean isFailed() {
        return (this.state == State.FAIL);
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
