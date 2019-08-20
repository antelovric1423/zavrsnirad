package com.lovricante.zavrsnirad;

import java.io.Serializable;
import java.util.ArrayList;

public class ActivityData implements Serializable {
    private String activityType;
    private long startTime;
    private long duration;
    private float distance;
    private ArrayList<TimePlace> timePlaces;

    public ActivityData(String activityType, long startTime, long duration, float distance, ArrayList<TimePlace> timePlaces) {
        this.activityType = activityType;
        this.startTime = startTime;
        this.duration = duration;
        this.distance = distance;
        this.timePlaces = timePlaces;
    }

    public String getActivityType() {
        return activityType;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }

    public ArrayList<TimePlace> getTimePlaces() {
        return this.timePlaces;
    }
}
