package com.lovricante.zavrsnirad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.util.Pair;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    static final int NEW_ACTIVITY_REQUEST = 1;
    private String newActivityType;

    private DatabaseHelper databaseHelper;
    private ActivityCardCreator cardCreator;

    private FloatingActionMenu FABMenu;
    private FloatingActionButton FABRun, FABWalk, FABDrive;

    private CardView directionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setButtonListeners();

        databaseHelper = new DatabaseHelper(this.getApplicationContext());
        cardCreator = new ActivityCardCreator();

        directionsContainer = findViewById(R.id.directionsContainer);
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

                /*
                if (directionsContainer.getVisibility() == View.VISIBLE) {
                    directionsContainer.setVisibility(View.GONE);
                }*/


                cardCreator.createActivityCardFromData(receivedData, this);
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

    public class ActivityCardCreator {
        private LinearLayout cardContainer;
        private CardView card;
        private LinearLayout cardLayout;
        private LineChart stChart;
        private LineChart vtChart;
        private TextView activityTypeTextView;
        private TextView activityStartTimeTextView;
        private TextView activityDurationTextView;
        private TextView activityDistanceTraveledTextView;
        private TextView averageVelocityTextView;
        private TextView maxVelocityTextView;

        public ActivityCardCreator() {
            cardContainer = findViewById(R.id.cardContainer);
        }

        public void createActivityCardFromData(ActivityData data, Context context) {
            Log.d("function_entry", "createActivityCardFromData");

            card = new CardView(context);
            cardLayout = new LinearLayout(context);
            stChart = new LineChart(context);
            vtChart = new LineChart(context);
            activityTypeTextView = new TextView(context);
            activityStartTimeTextView = new TextView(context);
            activityDurationTextView = new TextView(context);
            activityDistanceTraveledTextView = new TextView(context);
            averageVelocityTextView = new TextView(context);
            maxVelocityTextView = new TextView(context);

            setViewLayoutParams();
            processDataToViews(data);
            insertActivityEntryToLayout();
        }

        private void setViewLayoutParams() {
            Log.d("function_entry", "setViewLayoutParams");

            int dpValue = 8; // margin in dips
            float d = getResources().getDisplayMetrics().density;
            int margin = (int)(dpValue * d); // margin in pixels

            LinearLayout.LayoutParams basicLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLayoutParams.setMargins(margin, margin, margin, margin);
            LinearLayout.LayoutParams chartLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            chartLayoutParams.height = (int)(200*d);

            card.setLayoutParams(cardLayoutParams);

            stChart.setLayoutParams(chartLayoutParams);
            vtChart.setLayoutParams(chartLayoutParams);

            cardLayout.setLayoutParams(basicLayoutParams);
            cardLayout.setOrientation(LinearLayout.VERTICAL);

            activityTypeTextView.setLayoutParams(basicLayoutParams);
            activityStartTimeTextView.setLayoutParams(basicLayoutParams);
            activityDurationTextView.setLayoutParams(basicLayoutParams);
            activityDistanceTraveledTextView.setLayoutParams(basicLayoutParams);
            averageVelocityTextView.setLayoutParams(basicLayoutParams);
            maxVelocityTextView.setLayoutParams(basicLayoutParams);
        }

        private void processDataToViews(ActivityData data) {
            Log.d("function_entry", "processDataToViews " + data.getActivityType());

            activityTypeTextView.setText("Activity: " + data.getActivityType());
            Log.d("text","" + activityTypeTextView.getText());
            activityStartTimeTextView.setText("Date and time: " + getDateTimeStr(data.getStartTime()));
            Log.d("text","" +activityStartTimeTextView.getText() );
            activityDurationTextView.setText("Duration: " +
                    String.format("%02d:%02d", (int) (data.getDuration() / 60), (int) (data.getDuration() % 60)));
            Log.d("text","" + activityDurationTextView.getText());
            activityDistanceTraveledTextView.setText("Distance traveled: " + String.format("%.2f", data.getDistance()) + "m");
            Log.d("text",""+ activityDistanceTraveledTextView.getText());
            averageVelocityTextView.setText("Avg velocity: " + String.format("%.2f", data.getDistance() / data.getDuration()) + "m/s");
            Log.d("text",""+ averageVelocityTextView.getText());

            VelocityData velocityData = analyzeTimePlaceDataForVelocity(data.getTimePlaces());

            maxVelocityTextView.setText("Max velocity: " + String.format("%.2f", velocityData.getMaxVelocity()) + "m/s");
            Log.d("text","" + maxVelocityTextView.getText());

            setupLineChart(stChart, generateTimePlaceData(data.getTimePlaces()));
            setupLineChart(vtChart, generateVelocityData(velocityData));
            Log.d("function_exit","processDataToViews");
        }

        private void setupLineChart(LineChart lineChart, LineData chartData) {
            Log.d("function_entry", "setupLineChart");

            XAxis xAxis = lineChart.getXAxis();
            YAxis leftAxis = lineChart.getAxisLeft();

            lineChart.getDescription().setEnabled(false);
            lineChart.setBackgroundColor(Color.WHITE);
            lineChart.setDrawGridBackground(false);

            Legend l = lineChart.getLegend();
            l.setWordWrapEnabled(true);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);

            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setAxisMinimum(0f);
            xAxis.setGranularity(1f);
            xAxis.setAxisMaximum(chartData.getXMax());

            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f);

            lineChart.setData(chartData);
            lineChart.invalidate();
        }

        private LineData generateTimePlaceData(ArrayList<TimePlace> data) {
            Log.d("function_entry", "generateTimePlaceData");

            LineData d = new LineData();
            ArrayList<Entry> entries = new ArrayList<>();

            float currentDistance = 0;
            double prevPosLatitude;
            double prevPosLongitude;
            long prevPosTime;
            long timeDiffSeconds = 0;
            long currentPassedSeconds = 0;
            float[] distanceResult = new float[1];

            prevPosLatitude = data.get(0).getLatitude();
            prevPosLongitude = data.get(0).getLongitude();
            prevPosTime = data.get(0).getTime();

            entries.add(new Entry(0, 0));

            for (TimePlace it : data) {
                if (it.getTime() == prevPosTime) {
                    continue;
                }

                Location.distanceBetween(prevPosLatitude, prevPosLongitude,
                        it.getLatitude(), it.getLongitude(), distanceResult);

                timeDiffSeconds = (it.getTime() - prevPosTime) / 1000;
                if (timeDiffSeconds == 0)
                    continue;

                currentDistance = currentDistance + distanceResult[0];
                currentPassedSeconds = currentPassedSeconds + timeDiffSeconds;

                Log.d("generateVelocityData", "Add entry: " + currentDistance + ", " + currentPassedSeconds);
                entries.add(new Entry(currentPassedSeconds, currentDistance));

                prevPosLatitude = it.getLatitude();
                prevPosLongitude = it.getLongitude();
                prevPosTime = it.getTime();
            }

            LineDataSet set = new LineDataSet(entries, "Distance");
            set.setColor(Color.GREEN);
            set.setLineWidth(2.5f);
            set.setCircleColor(Color.GREEN);
            set.setCircleRadius(5f);
            set.setFillColor(Color.GREEN);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setDrawValues(true);
            set.setValueTextSize(10f);
            set.setValueTextColor(Color.GREEN);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            d.addDataSet(set);

            return d;
        }

        private LineData generateVelocityData(VelocityData data) {
            Log.d("function_entry", "generateVelocityData");

            LineData d = new LineData();
            ArrayList<Entry> entries = new ArrayList<>();

            entries.add(new Entry(0, 0));
            for (Pair<Float,Long> it : data.getVelocityInfo()) {
                Log.d("generateVelocityData", "Add entry: " + it.first + ", " + it.second);
                entries.add(new Entry(it.second, it.first));
            }

            LineDataSet set = new LineDataSet(entries, "Velocity");
            set.setColor(Color.MAGENTA);
            set.setLineWidth(2.5f);
            set.setCircleColor(Color.MAGENTA);
            set.setCircleRadius(5f);
            set.setFillColor(Color.MAGENTA);
            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setDrawValues(true);
            set.setValueTextSize(10f);
            set.setValueTextColor(Color.MAGENTA);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            d.addDataSet(set);

            return d;
        }

        private void insertActivityEntryToLayout() {
            Log.d("function_entry", "insertActivityEntryToLayout");

            cardLayout.addView(activityTypeTextView);
            cardLayout.addView(activityDurationTextView);
            cardLayout.addView(activityDistanceTraveledTextView);
            cardLayout.addView(averageVelocityTextView);
            cardLayout.addView(maxVelocityTextView);
            cardLayout.addView(stChart);
            cardLayout.addView(vtChart);
            card.addView(cardLayout);
            cardContainer.addView(card, 0);
        }

        private String getDateTimeStr(long timeMillis) {
            Log.d("function_entry", "getDateTimeStr");

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date resultDate = new Date(timeMillis);

            return sdf.format(resultDate);
        }

        private VelocityData analyzeTimePlaceDataForVelocity(ArrayList<TimePlace> timePlaces) {
            Log.d("function_entry", "analyzeTimePlaceDataForVelocity");
            ArrayList<Pair<Float, Long>> velocityInfo = new ArrayList<>();
            float maxVelocity = 0, currentVelocity = 0;
            double maxVelocityLatitude = 0, prevPosLatitude;
            double maxVelocityLongitude = 0, prevPosLongitude;
            long startTime;
            long prevPosTime;
            long timeDiffSeconds;
            float[] distanceResult = new float[1];

            prevPosLatitude = timePlaces.get(0).getLatitude();
            prevPosLongitude = timePlaces.get(0).getLongitude();
            startTime = prevPosTime = timePlaces.get(0).getTime();

            for (TimePlace it : timePlaces) {
                if (it.getTime() == prevPosTime) {
                    continue;
                }

                Location.distanceBetween(prevPosLatitude, prevPosLongitude,
                        it.getLatitude(), it.getLongitude(), distanceResult);
                timeDiffSeconds = (it.getTime() - prevPosTime) / 1000;
                if (timeDiffSeconds == 0)
                    continue;

                currentVelocity = distanceResult[0] / timeDiffSeconds;
                if (currentVelocity > maxVelocity) {
                    maxVelocity = currentVelocity;
                    maxVelocityLatitude = (prevPosLatitude + it.getLatitude()) / 2;
                    maxVelocityLongitude = (prevPosLongitude + it.getLongitude()) / 2;
                }

                velocityInfo.add(new Pair<>(new Float(currentVelocity), new Long((it.getTime() - startTime) / 1000)));

                prevPosLatitude = it.getLatitude();
                prevPosLongitude = it.getLongitude();
                prevPosTime = it.getTime();
            }

            return new VelocityData(velocityInfo, maxVelocity, maxVelocityLatitude, maxVelocityLongitude);
        }
    }

}
