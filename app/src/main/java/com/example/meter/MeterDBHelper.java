package com.example.meter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MeterDBHelper extends SQLiteOpenHelper {

    public static final String database = "meter.db";
    public static final int version = 1;

    public MeterDBHelper(@Nullable Context context) {
        super(context, database,null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ElectricMeterTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ElectricMeterTable.onUpgrade(db, oldVersion, newVersion);
    }
}
