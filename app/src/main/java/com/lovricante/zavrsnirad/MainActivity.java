package com.lovricante.zavrsnirad;

import android.content.Intent;
import android.os.Bundle;

import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    FloatingActionMenu FABMenu;
    FloatingActionButton FABRun, FABWalk, FABDrive;

    String newActivity;
    static final int NEW_ACTIVITY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FABMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FABWalk = (FloatingActionButton) findViewById(R.id.fab_walk);
        FABRun = (FloatingActionButton) findViewById(R.id.fab_run);
        FABDrive = (FloatingActionButton) findViewById(R.id.fab_drive);

        FABWalk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newActivity = "Walk";
                startTracking();
                startTracking();
            }
        });
        FABRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newActivity = "Run";
                startTracking();
            }
        });
        FABDrive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newActivity = "Drive";
                startTracking();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == NEW_ACTIVITY_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }

    public void startTracking() {
        Intent intent = new Intent(this, LocationTracker.class);
        startActivityForResult(intent, NEW_ACTIVITY_REQUEST);
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
