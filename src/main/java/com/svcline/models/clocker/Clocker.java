package com.svcline.models.clocker;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.Exclude;
import com.svcline.prodline.ProductLineConfiguration;

import java.util.ArrayList;

public class Clocker {
    protected String itemId;
    protected ProductLineConfiguration plConfiguration;   // The configuration used for this measurement
    protected ArrayList<Times> timesList = new ArrayList<>();
    @Exclude
    protected boolean timekeeping = false;

    public Clocker() {
    }

    public Clocker(String id, ProductLineConfiguration plConfiguration, boolean timekeeping) {
        this.itemId = id;
        this.plConfiguration = plConfiguration;
        this.timekeeping = timekeeping;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public ProductLineConfiguration getPlConfiguration() {
        return plConfiguration;
    }

    public void setPlConfiguration(ProductLineConfiguration plConfiguration) {
        this.plConfiguration = plConfiguration;
    }

    public ArrayList<Times> getTimesList() {
        return timesList;
    }

    public void setTimesList(ArrayList<Times> timesList) {
        this.timesList = timesList;
    }

    @Exclude
    public void production(String stationId) {
        if(this.timekeeping)
            addTime(stationId, Operation.STOP);
    }

    @Exclude
    public void preparation(String stationId) {
        if(this.timekeeping)
            addTime(stationId, Operation.START);
    }

    @Exclude
    public void addTime(String stationId, Operation operation) {
        Timing timing = new Timing(operation, Timestamp.now());

        if (timesList.size() == 0) {
            Times current = new Times(stationId);
            current.addUniqueTiming(timing);
            timesList.add(current);

            return;
        }

        int ix = getTimes(stationId);

        if (ix == -1) {
            // New Times entity. Create and add.
            Times current = new Times(stationId);
            current.addUniqueTiming(timing);
            timesList.add(current);
        } else {
            // Existing entity. Find and update.
            Times current = timesList.get(ix);

            current.addUniqueTiming(timing);
            timesList.set(ix, current);
        }
    }

    @Exclude
    public boolean isTimekeeping() {
        return timekeeping;
    }

    @Exclude
    public void setTimekeeping(boolean timekeeping) {
        this.timekeeping = timekeeping;
    }

    private int getTimes(String stationId) {
        int ix = 0;
        for (Times current : timesList) {
            if (stationId.equalsIgnoreCase(current.getStationId()))
                return ix;

            ix++;
        }

        return -1;
    }

    @Override
    public String toString() {
        return "Clocker{" +
               "id='" + itemId + '\'' +
               ", plConfiguration=" + plConfiguration +
               ", timesList=" + timesList +
               '}';
    }
}
