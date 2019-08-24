package com.lovricante.zavrsnirad;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ActivitiesData.db";
    public static final String TABLE_NAME_ACTIVITIES = "Activities";
    public static final String ACTIVITY_ID = "ActivityID";
    public static final String ACTIVITY_TYPE = "Type";
    public static final String DATE_TIME = "DateTime";
    public static final String DURATION = "Duration";
    public static final String DISTANCE = "Distance";

    public static final String TABLE_NAME_POSITION_DATA = "PositionData";
    public static final String POSITION_DATA_ID = "PositionDataID";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String TIMESTAMP = "Timestamp";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_ACTIVITIES + " (" +
                ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ACTIVITY_TYPE + " TEXT, " +
                DATE_TIME + " LONG NOT NULL, " +
                DURATION + " INTEGER NOT NULL, " +
                DISTANCE + " INTEGER NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_NAME_POSITION_DATA + " (" +
                POSITION_DATA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "FOREIGN KEY(" + ACTIVITY_ID + ") REFERENCES " + TABLE_NAME_ACTIVITIES +
                "(" + ACTIVITY_ID + "), " +
                LATITUDE + " REAL NOT NULL, " +
                LONGITUDE + " REAL NOT NULL, " +
                TIMESTAMP + " INTEGER NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ACTIVITIES);

        onCreate(db);
    }

    public void insertActivity(ActivityData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME_ACTIVITIES + " (" +
                ACTIVITY_TYPE + ", " + DATE_TIME + ", " + DURATION + ", " +
                DISTANCE + ") VALUES (" +
                "'" + data.getActivityType() + "', " +
                data.getStartTime() + ", " +
                data.getDuration() + ", " +
                data.getDistance() + ");");

        Cursor cursor = db.rawQuery("SELECT " + ACTIVITY_ID + " FROM " + TABLE_NAME_ACTIVITIES +
                " WHERE " + DATE_TIME + "=" + data.getStartTime(), null);

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
                    it.getTime() + "');");
        }
    }
}
