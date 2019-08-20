package com.lovricante.zavrsnirad;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static final int NEW_ACTIVITY_REQUEST = 1;
    DatabaseHelper databaseHelper;
    FloatingActionMenu FABMenu;
    FloatingActionButton FABRun, FABWalk, FABDrive;
    String newActivityType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setButtonListeners();

        databaseHelper = new DatabaseHelper(this.getApplicationContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == NEW_ACTIVITY_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                ActivityData receivedData = (ActivityData) data.getSerializableExtra("time_place_list");
                if (receivedData == null) {
                    Log.d("OnActivityResult", "Received Data is NULL");
                    return;
                }
                ArrayList<TimePlace> positionData = receivedData.getTimePlaces();
                if (positionData.size() < 2) {
                    Log.d("OnActivityResult", "Not enough entries in positionData to acknowledge activity");
                    return;
                }

                for (TimePlace it : positionData) {
                    Log.d("OnActivityResult", "positionData - time:" + it.getTime());
                }

                //databaseHelper.insertActivity(receivedData); TODO: ENABLE INSERTION OF ACTIVITIES AFTER DATA IS PROCESSED AND PRINTED OUT IN LAYOUT
            }
        }
    }

    public void startTracking() {
        Intent intent = new Intent(this, LocationTracker.class);
        intent.putExtra("activityType", newActivityType);

        startActivityForResult(intent, NEW_ACTIVITY_REQUEST);
    }

    private void setButtonListeners() {
        FABMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FABWalk = (FloatingActionButton) findViewById(R.id.fab_walk);
        FABRun = (FloatingActionButton) findViewById(R.id.fab_run);
        FABDrive = (FloatingActionButton) findViewById(R.id.fab_drive);

        FABWalk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newActivityType = "Walk";
                startTracking();
            }
        });
        FABRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newActivityType = "Run";
                startTracking();
            }
        });
        FABDrive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newActivityType = "Drive";
                startTracking();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
