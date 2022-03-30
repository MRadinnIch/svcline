package com.svcline.models;

public class Unit {
    private String action;
    private String jbId;
    private String organization;
    private String stationId;

    public Unit() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getJbId() {
        return jbId;
    }

    public void setJbId(String jbId) {
        this.jbId = jbId;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public boolean validate() {
        return !(this.action == null || this.jbId == null || this.organization == null || stationId == null);
    }

    @Override
    public String toString() {
        return "Unit{" +
               "action='" + action + '\'' +
               ", jbId='" + jbId + '\'' +
               ", organization='" + organization + '\'' +
               ", stationId='" + stationId + '\'' +
               '}';
    }
}
