package com.lovricante.zavrsnirad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ActivitiesData.db";
    public static final String TABLE_NAME_ACTIVITIES = "Activities";
    public static final String ACTIVITY_ID = "Activity_ID";
    public static final String ACTIVITY_TYPE = "Type";
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String DURATION = "Duration";
    public static final String DISTANCE = "Distance";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null,1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_ACTIVITIES + " (" +
                ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ACTIVITY_TYPE + " TEXT, " +
                DATE + " INTEGER NOT NULL, " +
                TIME + " INTEGER NOT NULL, " +
                DURATION + " INTEGER NOT NULL, " +
                DISTANCE + " INTEGER NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ACTIVITIES);

        onCreate(db);
    }
}
