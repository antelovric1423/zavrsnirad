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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
