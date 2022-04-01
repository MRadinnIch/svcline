package com.svcline.prodline;

import com.google.cloud.functions.HttpRequest;
import com.google.gson.Gson;
import com.svcline.models.State;

import java.io.IOException;

public class Transition {
    private static final Gson gson = new Gson();

    protected String id;
    protected String currentStationId;
    protected State state;

    public Transition() {
    }

    public Transition(HttpRequest request) throws IOException {
        Transition transition = gson.fromJson(request.getReader(), Transition.class);

        this.id = transition.id;
        this.currentStationId = transition.currentStationId;
        this.state = transition.state;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean validate() {
        return !(this.id == null || this.currentStationId == null);
    }
}
