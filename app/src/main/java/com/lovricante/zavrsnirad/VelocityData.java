package com.lovricante.zavrsnirad;

import androidx.core.util.Pair;

import java.util.ArrayList;

public class VelocityData {
    private ArrayList<Pair<Float, Long>> velocityInfo;
    private Float maxVelocity;
    private double maxVelocityLatitude;
    private double maxVelocityLongitude;

    public VelocityData(ArrayList<Pair<Float, Long>> velocityInfo, Float maxVelocity,
                        double maxVelocityLatitude, double maxVelocityLongitude) {
        this.velocityInfo = velocityInfo;
        this.maxVelocity = maxVelocity;
        this.maxVelocityLatitude = maxVelocityLatitude;
        this.maxVelocityLongitude = maxVelocityLongitude;
    }

    public ArrayList<Pair<Float, Long>> getVelocityInfo() {
        return velocityInfo;
    }

    public Float getMaxVelocity() {
        return maxVelocity;
    }

    public double getMaxVelocityLatitude() {
        return maxVelocityLatitude;
    }

    public double getMaxVelocityLongitude() {
        return maxVelocityLongitude;
    }
}

