package com.lovricante.zavrsnirad;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RouteDisplay extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);

        findViewById(R.id.timePassedTextView).setVisibility(View.GONE);
        findViewById(R.id.distancePassedTextView).setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(this);
        mMap = googleMap;
        int activityId = getIntent().getIntExtra("activityId", -1);
        if (activityId == -1) {
            Log.e("onMapReady", "Bad Activity ID");
            returnToMainActivity();
        }

        ActivityData activity = mDatabaseHelper.getActivityById(activityId);
        if (activity == null) {
            Log.e("onMapReady", "Bad Activity");
            returnToMainActivity();
        }

        boolean firstRun = true;
        TimePlace prevTimePlace = activity.getTimePlaces().get(0);
        for (TimePlace it : activity.getTimePlaces()) {
            if (firstRun) {
                mMap.addMarker(new MarkerOptions()
                        .position(it.getPlace()).title("Starting position"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it.getPlace(), 12f));
                firstRun = false;
                continue;
            }

            mMap.addPolyline(new PolylineOptions()
                    .add(prevTimePlace.getPlace(), it.getPlace())
                    .width(20)
                    .color(Color.CYAN)
                    .jointType(JointType.ROUND));

            prevTimePlace = it;
        }

        mMap.addMarker(new MarkerOptions()
                .position(prevTimePlace.getPlace()).title("Finish position"));
    }

    public void returnToMainActivity() {
        setResult(RESULT_OK);
        finish();
    }
}
