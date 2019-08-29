package com.lovricante.zavrsnirad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ActivitiesData.db";
    private static final String TABLE_NAME_ACTIVITIES = "Activities";
    private static final String ACTIVITY_ID = "ActivityID";
    private static final String ACTIVITY_TYPE = "Type";
    private static final String DURATION = "Duration";
    private static final String DISTANCE = "Distance";

    private static final String TABLE_NAME_POSITION_DATA = "PositionData";
    private static final String POSITION_DATA_ID = "PositionDataID";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String TIMESTAMP = "Timestamp";

    DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_POSITION_DATA);

        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_ACTIVITIES + " (" +
                ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ACTIVITY_TYPE + " TEXT, " +
                TIMESTAMP + " LONG NOT NULL, " +
                DURATION + " INTEGER NOT NULL, " +
                DISTANCE + " INTEGER NOT NULL);");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_POSITION_DATA + " (" +
                POSITION_DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LATITUDE + " REAL NOT NULL, " +
                LONGITUDE + " REAL NOT NULL, " +
                TIMESTAMP + " LONG NOT NULL, " +
                ACTIVITY_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + ACTIVITY_ID + ") REFERENCES " +
                TABLE_NAME_ACTIVITIES + "(" + ACTIVITY_ID + "));");
    }

    public void insertActivity(ActivityData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME_ACTIVITIES + " (" +
                ACTIVITY_TYPE + ", " + TIMESTAMP + ", " + DURATION + ", " +
                DISTANCE + ") VALUES (" +
                "'" + data.getActivityType() + "', " +
                data.getStartTime() + ", " +
                data.getDuration() + ", " +
                data.getDistance() + ");");

        Cursor cursor = db.rawQuery("SELECT " + ACTIVITY_ID + " FROM " + TABLE_NAME_ACTIVITIES +
                " WHERE " + TIMESTAMP + "=" + data.getStartTime(), null);

        if (cursor != null) {
            cursor.moveToFirst();
            int activityId = cursor.getInt(0);

            insertPositionData(activityId, data.getTimePlaces());
        }
    }

    private void insertPositionData(int activityId, ArrayList<TimePlace> data) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (TimePlace it : data) {
            db.execSQL("INSERT INTO " + TABLE_NAME_POSITION_DATA + " (" +
                    ACTIVITY_ID + ", " + LATITUDE + ", " + LONGITUDE + ", " +
                    TIMESTAMP + ") VALUES (" +
                    activityId + ", " +
                    it.getLatitude() + ", " +
                    it.getLongitude() + ", " +
                    it.getTime() + ");");
        }
    }

    public ArrayList<ActivityData> getAllActivities() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<ActivityData> storedData = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_ACTIVITIES, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                int activityId = cursor.getInt(cursor.getColumnIndex(ACTIVITY_ID));
                String activityType = cursor.getString(cursor.getColumnIndex(ACTIVITY_TYPE));
                long timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
                int duration = cursor.getInt(cursor.getColumnIndex(DURATION));
                int distance = cursor.getInt(cursor.getColumnIndex(DISTANCE));

                storedData.add(new ActivityData(activityType, timestamp, duration, distance, getActivityPositions(activityId)));
                cursor.moveToNext();
            }
        }
        return storedData;
    }

    private ArrayList<TimePlace> getActivityPositions(int activityId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TimePlace> timePlaces = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_POSITION_DATA +
                " WHERE " + ACTIVITY_ID + "=" + activityId, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                float latitude = cursor.getFloat(cursor.getColumnIndex(LATITUDE));
                float longitude = cursor.getFloat(cursor.getColumnIndex(LONGITUDE));
                long timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));

                timePlaces.add(new TimePlace(new LatLng(latitude, longitude), timestamp));
                cursor.moveToNext();
            }
        }

        return timePlaces;
    }
}
