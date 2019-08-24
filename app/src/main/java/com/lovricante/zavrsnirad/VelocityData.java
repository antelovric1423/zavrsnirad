package com.lovricante.zavrsnirad;

import androidx.core.util.Pair;

import java.util.ArrayList;

public class VelocityData {
    private ArrayList<Pair<Float, Long>> velocityInfo;
    private Float maxVelocity;

    public VelocityData(ArrayList<Pair<Float, Long>> velocityInfo, Float maxVelocity) {
        this.velocityInfo = velocityInfo;
        this.maxVelocity = maxVelocity;
    }

    public ArrayList<Pair<Float, Long>> getVelocityInfo() {
        return velocityInfo;
    }

    public Float getMaxVelocity() {
        return maxVelocity;
    }
}

