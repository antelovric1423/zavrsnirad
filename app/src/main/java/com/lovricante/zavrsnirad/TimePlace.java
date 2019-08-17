package com.lovricante.zavrsnirad;

import com.google.android.gms.maps.model.LatLng;

public class TimePlace {
    private LatLng place;
    private long time;

    public TimePlace(LatLng place, long time) {
        this.place = place;
        this.time = time;
    }

    public LatLng getPlace() {
        return place;
    }

    public void setPlace(LatLng place) {
        this.place = place;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
