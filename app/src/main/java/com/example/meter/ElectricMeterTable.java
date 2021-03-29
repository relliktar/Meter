package com.example.meter;

import android.database.sqlite.SQLiteDatabase;

public class ElectricMeterTable {
    public static final String TABLE_ELECTRIC = "electric";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SPAN = "span_of_days";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_READINGS = "meter_readings";
    public static final String COLUMN_CONSUMED = "consumed";
    public static final String COLUMN_AVERAGE = "average_consumption";

    public static final String CREATE = " CREATE TABLE " + TABLE_ELECTRIC + " ( " +
            COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT , " +
            COLUMN_SPAN +" INTEGER , " +
            COLUMN_DATE + " DATE NOT NULL , " +
            COLUMN_READINGS + " INTEGER NOT NULL , " +
            COLUMN_CONSUMED + " INTEGER , " +
            COLUMN_AVERAGE + " REAL " +
            " ); ";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
