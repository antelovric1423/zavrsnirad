package com.lovricante.zavrsnirad;

import java.io.Serializable;
import java.util.ArrayList;

public class TimePlaceArrayListWrapper implements Serializable {
    private ArrayList<TimePlace> timePlaces;

    public TimePlaceArrayListWrapper(ArrayList<TimePlace> data) {
        this.timePlaces = data;
    }

    public ArrayList<TimePlace> getTimePlaces() {
        return this.timePlaces;
    }
}
