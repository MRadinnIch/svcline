package com.svcline.models.clocker;

import com.google.cloud.firestore.annotation.Exclude;

import java.util.ArrayList;

public class Times {
    private String stationId;
    private ArrayList<Timing> timingList;

    public Times() {
    }

    public Times(String stationId) {
        this.stationId = stationId;
        timingList = new ArrayList<>();
    }

    public Times(String stationId, ArrayList<Timing> timing) {
        this.stationId = stationId;
        this.timingList = timing;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public ArrayList<Timing> getTimingList() {
        return timingList;
    }

    public void setTimingList(ArrayList<Timing> timingList) {
        this.timingList = timingList;
    }

    //// Methods ////

    @Exclude
    public boolean operationExists(Operation currentOperation) {
        for (Timing current : timingList) {
            if (current.getOperation() == currentOperation)
                return true;
        }

        return false;
    }

    @Exclude
    public boolean operationPrepared(Operation currentOperation) {
        return !(currentOperation == Operation.PRODUCTION && timingList.size() == 0);
    }

    @Exclude
    public void addUniqueTiming(Timing timing) {
        if (timing.getTimestamp() == null || timing.getOperation() == null)
            throw new IllegalArgumentException("Incorrect timing value provided for addition.");
        else if (operationExists(timing.getOperation()))
            throw new IllegalArgumentException("Operation exists for station: " + stationId);
        else if (!operationPrepared(timing.getOperation()))
            throw new IllegalArgumentException("Operation has not been prepared.");

        timingList.add(timing);
    }

    @Override
    public String toString() {
        return "Times{" +
               "stationId='" + stationId + '\'' +
               ", timingList=" + timingList +
               '}';
    }
}
