package com.lovricante.zavrsnirad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.clans.fab.FloatingActionButton;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    static final int NEW_ACTIVITY_REQUEST = 1;
    static final int SHOW_ROUTE_REQUEST = 2;

    private DatabaseHelper mDatabaseHelper;
    private ActivityCardCreator mCardCreator;

    private FloatingActionButton FABRun, FABWalk, FABDrive;
    private CardView directionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViewReferences();

        mDatabaseHelper = new DatabaseHelper(this.getApplicationContext());
        mCardCreator = new ActivityCardCreator();

        ArrayList<ActivityData> activityList = mDatabaseHelper.getAllActivities();
        for (ActivityData it : activityList) {
            if (it.getTimePlaces().size() != 0) {
                processActivityData(it);
            }
        }
    }

    private void setViewReferences() {
        FABWalk = (FloatingActionButton) findViewById(R.id.fab_walk);
        FABRun = (FloatingActionButton) findViewById(R.id.fab_run);
        FABDrive = (FloatingActionButton) findViewById(R.id.fab_drive);

        directionsContainer = findViewById(R.id.directionsContainer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FABWalk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTracking("Walk");
            }
        });
        FABRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTracking("Run");
            }
        });
        FABDrive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTracking("Drive");
            }
        });
    }

    public void startTracking(String activityType) {
        Intent intent = new Intent(this, LocationTracker.class);
        intent.putExtra("activityType", activityType);

        startActivityForResult(intent, NEW_ACTIVITY_REQUEST);
    }

    public void showRoute(View button) {
        int activityId = ((View) button.getParent()).getId();

        Intent intent = new Intent(this, RouteDisplay.class);
        intent.putExtra("activityId", activityId);
        startActivityForResult(intent, SHOW_ROUTE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                int receivedData =
                        (int) data.getSerializableExtra("ActivityId");
                if (receivedData < 0) {
                    Log.e("OnActivityResult", "Bad Activity Data!");
                    return;
                }

                processActivityData(mDatabaseHelper.getActivityById(receivedData));
            }
        }
    }

    private void processActivityData(ActivityData data) {
        if (directionsContainer.getVisibility() == View.VISIBLE) {
            directionsContainer.setVisibility(View.INVISIBLE);
        }

        mCardCreator.createActivityCardFromData(data, this);
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
        ArrayList<Entry> mPaceEntries, mDistanceEntries;
        private LinearLayout cardContainer;
        private CardView card;
        private LinearLayout cardLayout;
        private LineChart distanceTravelledChart;
        private LineChart paceChart;
        private TextView activityTypeTextView;
        private TextView activityStartTimeTextView;
        private TextView activityDurationTextView;
        private TextView activityDistanceTraveledTextView;
        private TextView averagePaceTextView;
        private TextView averageVelocityTextView;
        private Button showRouteButton;
        private int mActivityId;
        private String mActivityType = "";
        private long mStartTime = 0;
        private float mDuration = 0, mDistance = 0;

        public ActivityCardCreator() {
            cardContainer = findViewById(R.id.cardContainer);
        }

        public void createActivityCardFromData(ActivityData data, Context context) {
            Log.d("function_entry", "createActivityCardFromData");

            card = new CardView(context);
            cardLayout = new LinearLayout(context);
            distanceTravelledChart = new LineChart(context);
            paceChart = new LineChart(context);
            activityTypeTextView = new TextView(context);
            activityStartTimeTextView = new TextView(context);
            activityDurationTextView = new TextView(context);
            activityDistanceTraveledTextView = new TextView(context);
            averagePaceTextView = new TextView(context);
            averageVelocityTextView = new TextView(context);
            showRouteButton = new Button(context);

            processActivityData(data);
            setupViews();
            setViewLayoutParams();
            insertActivityEntryToLayout();
        }

        private void processActivityData(ActivityData activity) {
            mActivityId = activity.getActivityId();
            mActivityType = activity.getActivityType();
            mDistanceEntries = new ArrayList<>();
            mPaceEntries = new ArrayList<>();
            mDistance = 0;

            ArrayList<TimePlace> data = activity.getTimePlaces();

            float timeDiffSeconds, timePassed = 0;
            float[] distanceResult = new float[1];
            double prevPosLatitude = data.get(0).getLatitude();
            double prevPosLongitude = data.get(0).getLongitude();
            long prevPosTime = mStartTime = data.get(0).getTime();
            mDuration = (float) (data.get(data.size() - 1).getTime() - mStartTime) / 1000;

            mDistanceEntries.add(new Entry(0, 0));
            mPaceEntries.add(new Entry(0, 0));

            for (TimePlace it : data) {
                timeDiffSeconds = (float) (it.getTime() - prevPosTime) / 1000;
                if (timeDiffSeconds == 0)
                    continue;

                Location.distanceBetween(prevPosLatitude, prevPosLongitude, it.getLatitude(),
                        it.getLongitude(), distanceResult);
                mDistance = mDistance + distanceResult[0];
                timePassed = timePassed + timeDiffSeconds;

                mDistanceEntries.add(new Entry(timePassed, mDistance));
                mDistanceEntries.add(new Entry(timePassed, mDistance));

                prevPosLatitude = it.getLatitude();
                prevPosLongitude = it.getLongitude();
                prevPosTime = it.getTime();
            }
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        private void setupViews() {
            activityTypeTextView.setText("Activity: " + mActivityType);
            activityStartTimeTextView.setText("Date and time: " + getDateTimeStr(mStartTime));
            activityDurationTextView.setText("Duration: " +
                    String.format("%02d:%02d", (int) (mDuration / 60), (int) (mDuration % 60)));
            activityDistanceTraveledTextView.setText("Distance traveled: " +
                    String.format("%.2f", mDistance) + "m");
            averagePaceTextView.setText("Average pace: " +
                    String.format("%.2f", (mDuration / 60) / (mDistance / 1000)) + "min/km");
            averageVelocityTextView.setText("Average velocity: " +
                    String.format("%.2f", (mDistance / 1000) / (mDuration / 3600)) + "km/h");
            showRouteButton.setText("Show route");

            setupLineChart(distanceTravelledChart, generateDistanceTravelledData());
            setupLineChart(paceChart, generatePaceData());
        }

        private void setViewLayoutParams() {
            Log.d("function_entry", "setViewLayoutParams");

            float d = getResources().getDisplayMetrics().density;
            int dpValue = 8; // margin in dips
            int margin = (int) (dpValue * d); // margin in pixels
            LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLayoutParams.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(cardLayoutParams);
            card.setRadius((int) (4 * d));

            LinearLayout.LayoutParams chartLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            chartLayoutParams.height = (int) (200 * d);
            distanceTravelledChart.setLayoutParams(chartLayoutParams);
            paceChart.setLayoutParams(chartLayoutParams);

            cardLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            cardLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textLayoutParams.leftMargin = (int) (4 * d);
            activityTypeTextView.setLayoutParams(textLayoutParams);
            activityStartTimeTextView.setLayoutParams(textLayoutParams);
            activityDurationTextView.setLayoutParams(textLayoutParams);
            activityDistanceTraveledTextView.setLayoutParams(textLayoutParams);
            averagePaceTextView.setLayoutParams(textLayoutParams);
            averageVelocityTextView.setLayoutParams(textLayoutParams);
            showRouteButton.setLayoutParams(textLayoutParams);
        }

        private void insertActivityEntryToLayout() {
            Log.d("function_entry", "insertActivityEntryToLayout");

            cardLayout.addView(activityTypeTextView);
            cardLayout.addView(activityStartTimeTextView);
            cardLayout.addView(activityDurationTextView);
            cardLayout.addView(activityDistanceTraveledTextView);
            cardLayout.addView(averagePaceTextView);
            cardLayout.addView(averageVelocityTextView);
            cardLayout.addView(distanceTravelledChart);
            cardLayout.addView(paceChart);
            cardLayout.addView(showRouteButton);
            cardLayout.setId(mActivityId);
            card.addView(cardLayout);
            cardContainer.addView(card, 0);

            showRouteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRoute(v);
                }
            });
        }

        private String getDateTimeStr(long timeMillis) {
            Log.d("function_entry", "getDateTimeStr");

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date resultDate = new Date(timeMillis);

            return sdf.format(resultDate);
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

            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format(Locale.getDefault(),
                            "%02d:%02d", value / 60, value % 60);
                }
            });

            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f);

            lineChart.getAxisRight().setEnabled(false);

            lineChart.setData(chartData);
            lineChart.invalidate();
        }

        private LineData generateDistanceTravelledData() {
            LineData d = new LineData();

            LineDataSet set = new LineDataSet(mDistanceEntries,
                    "x:time(min:sec), y:distance travelled(m)");
            set.setColor(Color.GREEN);
            set.setLineWidth(2.5f);
            set.setDrawCircles(false);
            set.setFillColor(Color.GREEN);
            set.setDrawFilled(true);
            set.setDrawValues(true);
            set.setValueTextSize(10f);
            set.setValueTextColor(Color.GREEN);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            d.addDataSet(set);

            return d;
        }

        private LineData generatePaceData() {
            LineData d = new LineData();

            LineDataSet set = new LineDataSet(mPaceEntries,
                    "x:time(min:sec), y:Pace(min/km)");
            set.setColor(Color.MAGENTA);
            set.setLineWidth(2.5f);
            set.setDrawCircles(false);
            set.setFillColor(Color.MAGENTA);
            set.setDrawFilled(true);
            set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            set.setDrawValues(true);
            set.setValueTextSize(10f);
            set.setValueTextColor(Color.MAGENTA);
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            d.addDataSet(set);

            return d;
        }
    }

}
