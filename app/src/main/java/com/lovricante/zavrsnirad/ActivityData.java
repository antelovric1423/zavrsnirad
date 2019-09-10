package com.lovricante.zavrsnirad;

import java.util.ArrayList;

public class ActivityData {
    private int activityId;
    private String activityType;
    private ArrayList<TimePlace> timePlaces;

    public ActivityData(int id,
                        String activityType,
                        ArrayList<TimePlace> timePlaces) {
        this.activityId = id;
        this.activityType = activityType;
        this.timePlaces = timePlaces;
    }

    public int getActivityId() {
        return activityId;
    }

    public String getActivityType() {
        return activityType;
    }

    public ArrayList<TimePlace> getTimePlaces() {
        return this.timePlaces;
    }
}
