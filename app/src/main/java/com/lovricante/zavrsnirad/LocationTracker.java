package com.lovricante.zavrsnirad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Locale;

public class LocationTracker extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private static final String TAG = "LocationTracker";
    private static final long UPDATE_INTERVAL = 1000;  /* 1 sec */
    private static final long FASTEST_INTERVAL = 500; /* 0.5 sec */
    Handler timerHandler;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private TimePlace mPrevTimePlace;
    private LatLng mCurrentLocation;
    private DatabaseHelper mDatabaseHelper;
    private String mActivityType;
    private long mStartTime, mCurrentTime;
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long timeDiff = System.currentTimeMillis() - mStartTime;
            int seconds = (int) (timeDiff / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timeTrackerTextView.setText(String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };
    private float mTotalDistance = 0, mMinimalDistanceToTrack;
    private boolean mLocationAccessPermitted, isFirstRun = true, startPressed = false;
    private int mEntryId = -1;
    private FloatingActionButton finishButton;
    private TextView distanceTrackerTextView, timeTrackerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);

        mActivityType = getIntent().getStringExtra("activityType");
        switch (mActivityType) {
            case "Walk":
                mMinimalDistanceToTrack = 2;
                break;
            case "Run":
                mMinimalDistanceToTrack = 1;
                break;
            case "Drive":
                mMinimalDistanceToTrack = 5;
                break;
            default:
                mMinimalDistanceToTrack = 1;
        }

        if (requestLocationPermission()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            if (!checkIsLocationEnabled()) {
                returnToMainActivity();
            }
        } else {
            returnToMainActivity();
        }

        mDatabaseHelper = new DatabaseHelper(this.getApplicationContext());

        timeTrackerTextView = findViewById(R.id.timePassedTextView);
        distanceTrackerTextView = findViewById(R.id.distancePassedTextView);
        finishButton = findViewById(R.id.fab_finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (startPressed) {
                    returnToMainActivity();
                } else {
                    startPressed = true;
                    finishButton.setImageDrawable(getResources()
                            .getDrawable(android.R.drawable.checkbox_on_background));

                    mStartTime = System.currentTimeMillis();
                    timerHandler = new Handler();
                    timerHandler.postDelayed(timerRunnable, 0);

                    if (mMap != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(mCurrentLocation).title("Starting position"));
                    }
                    mPrevTimePlace = new TimePlace(mCurrentLocation, mCurrentTime);
                }
            }
        });
    }

    public void returnToMainActivity() {
        Log.d(TAG, "Stop timer, return to main activity and send data");

        timerHandler.removeCallbacks(timerRunnable);

        if (!startPressed || mEntryId < 0) {
            setResult(RESULT_CANCELED);
            finish();
        }

        Intent data = new Intent(this, MainActivity.class);
        data.putExtra("ActivityId", mEntryId);

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        float[] distanceResult = new float[1];
        mCurrentTime = System.currentTimeMillis();
        mMap = googleMap;

        if (mCurrentLocation != null) {
            if (isFirstRun) {
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 16f));

                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }

                isFirstRun = false;
                finishButton.setVisibility(View.VISIBLE);
            } else if (startPressed) {
                Location.distanceBetween(mPrevTimePlace.getLatitude(), mPrevTimePlace.getLongitude(),
                        mCurrentLocation.latitude, mCurrentLocation.longitude, distanceResult);

                if (distanceResult[0] > mMinimalDistanceToTrack) {
                    if (mEntryId == -1) {
                        mEntryId = mDatabaseHelper.insertActivity(mActivityType);

                        mDatabaseHelper.insertPositionData(mEntryId, mPrevTimePlace);
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                    mMap.addPolyline(new PolylineOptions()
                            .add(mPrevTimePlace.getPlace(), mCurrentLocation)
                            .width(20)
                            .color(Color.CYAN)
                            .jointType(JointType.ROUND));

                    mPrevTimePlace = new TimePlace(mCurrentLocation, mCurrentTime);
                    mDatabaseHelper.insertPositionData(mEntryId, mPrevTimePlace);

                    mTotalDistance = mTotalDistance + distanceResult[0];
                    distanceTrackerTextView.setText(String
                            .format(Locale.getDefault(), "Distance: %.2fm", mTotalDistance));
                }
            }
        }
    }

    @Override
    public void onPause() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation == null) {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }


    private boolean checkIsLocationEnabled() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private boolean isLocationEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean requestLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(LocationTracker.this, "Single permission is granted!", Toast.LENGTH_SHORT).show();
                        mLocationAccessPermitted = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            mLocationAccessPermitted = false;
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        return mLocationAccessPermitted;
    }

}
