package com.svcline.models.clocker.bq;

import com.svcline.models.Station;
import com.svcline.models.clocker.Clocker;
import com.svcline.models.clocker.Operation;
import com.svcline.models.clocker.Times;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BqEntry extends Clocker {
    private long itemProductionTime;
    // HashMap that contains the productions times per station.
    // <K> is the index corresponding to the index from the "timesList" in the parent object.
    // <V> is the calculated time it took to start the item production (previous stop - current start).
    private final HashMap<Integer, Double> stationProductionLeadTimes;
    private final HashMap<Integer, Double> stationProductionTimes;
    private final HashMap<Integer, Double> estimatedStationProductionTimes;
    private final HashMap<Integer, Double> stationProductionTimeDeviations;
    private Double estimatedItemProductionTime;
    private Double itemProductionTimeDeviation;

    public BqEntry() {
        super();
        this.stationProductionLeadTimes = new HashMap<>();
        this.stationProductionTimes = new HashMap<>();
        this.estimatedStationProductionTimes = new HashMap<>();
        this.stationProductionTimeDeviations = new HashMap<>();
        this.estimatedItemProductionTime = 0.0;
        this.itemProductionTimeDeviation = 0.0;
    }

    public Double getEstimatedItemProductionTime() {
        return estimatedItemProductionTime;
    }

    public void setEstimatedItemProductionTime(Double estimatedItemProductionTime) {
        this.estimatedItemProductionTime = estimatedItemProductionTime;
    }

    public Double getItemProductionTimeDeviation() {
        return itemProductionTimeDeviation;
    }

    public void setItemProductionTimeDeviation(Double itemProductionTimeDeviation) {
        this.itemProductionTimeDeviation = itemProductionTimeDeviation;
    }

    public static BqEntry from(Clocker clocker) {
        BqEntry bqEntry = new BqEntry();

        bqEntry.setItemId(clocker.getItemId());
        bqEntry.setPlConfiguration(clocker.getPlConfiguration());
        bqEntry.setTimesList(clocker.getTimesList());
        bqEntry.setTimekeeping(clocker.isTimekeeping());

        return bqEntry;
    }

    public void calculateItemProductionTime() {
        ArrayList<Times> timesList = this.getTimesList();

        if (timesList.isEmpty())
            return;

        // Start timestamp is at first entry first time.
        Date start = timesList.get(0).getTimingList().get(Operation.START.getValue()).getTimestamp().toDate();
        // Stop timestamp is at last entry second time
        Date stop = timesList.get(timesList.size() - 1).getTimingList().get(Operation.STOP.getValue()).getTimestamp().toDate();

        // Working with Google Timestamp is a mess, easier method to get duration is to use Date, with some conversion
        itemProductionTime = getDurationInNanosFromMilli(start.getTime(), stop.getTime());

        if (this.estimatedItemProductionTime > 0) {
            this.itemProductionTimeDeviation = round(getItemProductionTimeInSeconds(itemProductionTime) - estimatedItemProductionTime.longValue());
        }
    }

    public void calculateStationTimes() {
        ArrayList<Times> timesList = this.getTimesList();

        if (timesList.isEmpty())
            return;

        // We cannot calculate the lead time for first item
        for (int i = 0; i < timesList.size(); i++) {
            Times times = timesList.get(i);

            long currentStationStartTime = times.getTimingList().get(Operation.START.getValue()).getTimestamp().toDate().getTime();
            long currentStationStopTime = times.getTimingList().get(Operation.STOP.getValue()).getTimestamp().toDate().getTime();

            if (i > 0) {
                long previousStationStopTime = timesList.get(i - 1).getTimingList().get(Operation.STOP.getValue()).getTimestamp().toDate().getTime();

                long stationLeadTime = getDurationInNanosFromMilli(previousStationStopTime, currentStationStartTime);
                this.stationProductionLeadTimes.put(i, getItemProductionTimeInSeconds(stationLeadTime));
            }

            long stationProductionTime = getDurationInNanosFromMilli(currentStationStartTime, currentStationStopTime);
            this.stationProductionTimes.put(i, getItemProductionTimeInSeconds(stationProductionTime));

            Station station = this.plConfiguration.getConfiguredStationMap().getStationById(times.getStationId());
            this.estimatedStationProductionTimes.put(i, station.getEstimatedStationProductionTime());
            this.stationProductionTimeDeviations.put(i, round(getItemProductionTimeInSeconds(stationProductionTime) -
                                                        station.getEstimatedStationProductionTime()));
        }
    }

    public Double getStationProductionTimeFor(int pos) {
        return this.stationProductionTimes.get(pos);
    }

    public Double getStationProductionLeadTimeFor(int pos) {
        return this.stationProductionLeadTimes.get(pos);
    }

    public Double getEstimatedStationProductionTimeFor(int pos) {
        return this.estimatedStationProductionTimes.get(pos);
    }

    public Double getStationProductionTimeDeviationFor(int pos) {
        return this.stationProductionTimeDeviations.get(pos);
    }

    public long getItemProductionTime() {
        return itemProductionTime;
    }

    public double getItemProductionTimeInSeconds() {
        return getItemProductionTimeInSeconds(this.itemProductionTime);
    }

    private long getDurationInNanosFromMilli(long start, long stop) {
        return TimeUnit.NANOSECONDS.convert(stop - start, TimeUnit.MILLISECONDS);
    }

    private double getItemProductionTimeInSeconds(long value) {
        return (double) value / 1_000_000_000;
    }

    private Double round(Double num) {
        return Math.round(num * 100.0) / 100.0;
    }
}
