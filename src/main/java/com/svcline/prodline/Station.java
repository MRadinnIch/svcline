package com.svcline.prodline;

public class Station {
    private String id;
    private String name;
    private String stationType;
    private String nextStationId;
    private allowedStates allowedStates;

    public Station(String id, String name, String stationType, String nextStationId) {
        this.id = id;
        this.name = name;
        this.stationType = stationType;
        this.nextStationId = nextStationId;
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

    public String getStationType() {
        return stationType;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public String getNextStationId() {
        return nextStationId;
    }

    public void setNextStationId(String nextStationId) {
        this.nextStationId = nextStationId;
    }
}
