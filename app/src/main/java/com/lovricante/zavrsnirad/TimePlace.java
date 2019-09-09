package com.lovricante.zavrsnirad;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class TimePlace implements Serializable {
    private LatLng place;
    private long time;

    public TimePlace(LatLng place, long time) {
        this.place = place;
        this.time = time;
    }

    public LatLng getPlace() {
        return place;
    }

    public double getLatitude() {
        return place.latitude;
    }

    public double getLongitude() {
        return place.longitude;
    }

    public long getTime() {
        return time;
    }
}
