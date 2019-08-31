package com.lovricante.zavrsnirad;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class TimePlace implements Serializable {
    private double latitude;
    private double longitude;
    private long time;

    public TimePlace(LatLng place, long time) {
        this.latitude = place.latitude;
        this.longitude = place.longitude;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTime() {
        return time;
    }
}
