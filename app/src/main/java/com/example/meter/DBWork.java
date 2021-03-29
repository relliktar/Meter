package com.example.meter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DBWork {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private static final ContentValues contentValues = new ContentValues();

    // Формируем строку с датой перед занесением в базу.
    static String getDate(TextView textView) {
        String[] dat = textView.getText().toString().split("\\.");
        return dat[2] + "-" + dat[1] + "-" + dat[0];
    }

    // Получаем из базы наименьшую ближайшую дату к данной.
    private static Cursor getMax(String dat, MeterDBHelper helper) {
        return helper.getReadableDatabase().query(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                null,
                ElectricMeterTable.COLUMN_DATE + " > '" + dat + "' ",
                null,
                null,
                null,
                ElectricMeterTable.COLUMN_DATE + " ASC ",
                " 1 "
        );
    }

    // Получение из базы наибольшей ближайшей даты к данной.
    private static Cursor getMin(String dat, MeterDBHelper helper) {
        return helper.getReadableDatabase().query(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                null,
                ElectricMeterTable.COLUMN_DATE + " < '" + dat + "' ",
                null,
                null,
                null,
                ElectricMeterTable.COLUMN_DATE + " DESC ",
                " 1 "
        );
    }

    // Получение количества дней между датами.
    private static long getDays(Cursor cursor, String stringDate) {
        long day = 0;
        try {
            String cursorDate = cursor.getString(2).trim();
            long dateCursor = Objects.requireNonNull(format.parse(cursorDate)).getTime();
            long dateString = Objects.requireNonNull(format.parse(stringDate)).getTime();
            day = (Math.abs(dateString - dateCursor)) / (24 * 60 * 60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Возможно ли возвращение "0"?
        return day;
    }

    // Внесение в базу данных новой строки.
    static void miMax(Context context, String dat, int meter_reading) {
        MeterDBHelper helper = new MeterDBHelper(context);

        // Получаем ближайшие даты к переданной.
        Cursor max = getMax(dat, helper);
        Cursor min = getMin(dat, helper);

        long day;

        // Если есть дата раньше, то
        if (min.moveToFirst()) {
            int min_pokaz = min.getInt(3);
            double potrebleno = meter_reading - min_pokaz;
            day = getDays(min, dat);
            double sred_potreb = potrebleno / day;

            // добавляем в базу новую строку с новыми данными.
            add(helper, day, dat, meter_reading, potrebleno, sred_potreb);
        }
        // Если есть дата позже, то
        if (max.moveToFirst()) {
            int max_pokaz = max.getInt(3);
            double potrebleno = max_pokaz - meter_reading;
            day = getDays(max, dat);
            double sred_potreb = potrebleno / day;

            // если даты раньше нет, то вносим новую строку с новыми данными.
            if (min.getCount() == 0) add(helper, dat, meter_reading);

            // обновляем строку с ближайшей следующей датой, добавляя количество дней,
            // потребление и среднее потребление за эти дни.
            update(helper, max.getInt(0), day, potrebleno, sred_potreb);

        }

        // Если нет дат позже и раньше, то просто добавляем новую строку.
        // Обычно это только первая строка.
        if (!min.moveToFirst() && !max.moveToFirst()) {
            add(helper, dat, meter_reading);
        }
        min.close();
        max.close();
        helper.close();
    }

    // Обновляем строку с переданным ID новыми датой и показаниями.
    static void updateLine(Context context, String databaseId, String dat, int meter_reading) {
        MeterDBHelper helper = new MeterDBHelper(context);
        int id = Integer.parseInt(databaseId);

        // Получаем ближайшие даты к переданной.
        Cursor max = getMax(dat, helper);
        Cursor min = getMin(dat, helper);
        long day;

        // Если есть дата раньше переданной, то
        if (min.moveToFirst()) {
            day = getDays(min, dat);
            int min_pokaz = min.getInt(3);
            double potrebleno = meter_reading - min_pokaz;
            double sred_potreb = potrebleno / day;

            // обновляем строку с данным ID, внеся новые значения прошедших дней, датой,
            // показаниями, потреблением и средним потреблением.
            update(helper, id, day, dat, meter_reading, potrebleno, sred_potreb);
        }

        // Если есть дата позже переданной, то
        if (max.moveToFirst()) {
            int max_pokaz = max.getInt(3);
            double potrebleno = max_pokaz - meter_reading;
            day = getDays(max, dat);
            double sred_potreb = potrebleno / day;

            // если даты ранее нет, обновляем строку с заданным ID
            // новыми значениями даты и показаний
            if (min.getCount() == 0) update(helper, id, dat, meter_reading);

            // обновляем строку с ближайшей следующей датой новыми значениями
            // количества прошедших дней, потребления и среднего потребления.
            update(helper, max.getInt(0), day, potrebleno, sred_potreb);
        }

        // Если строк с датами раньше и позже нет, то
        if (!min.moveToFirst() && !max.moveToFirst()) {
            // обновляем строку с заданным ID новыми значениями даты и показаний
            update(helper, id, dat, meter_reading);
        }
        min.close();
        max.close();
        helper.close();
    }

    // Вносим новую строку в базу с датой и показаниями.
    private static void add(MeterDBHelper helper, String date, int readings) {
        contentValues.clear();
        contentValues.put(ElectricMeterTable.COLUMN_DATE, date);
        contentValues.put(ElectricMeterTable.COLUMN_READINGS, readings);
        helper.getWritableDatabase().insert(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                null,
                contentValues
        );
    }

    // Добавляем в базу новую строку с количеством дней, датой, показаниями, потреблением и средним потреблением.
    private static void add(MeterDBHelper helper, long span, String date, int readings, double consumed, double average) {
        contentValues.clear();
        contentValues.put(ElectricMeterTable.COLUMN_SPAN, span);
        contentValues.put(ElectricMeterTable.COLUMN_DATE, date);
        contentValues.put(ElectricMeterTable.COLUMN_READINGS, readings);
        contentValues.put(ElectricMeterTable.COLUMN_CONSUMED, consumed);
        contentValues.put(ElectricMeterTable.COLUMN_AVERAGE, average);
        helper.getWritableDatabase().insert(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                null,
                contentValues
        );
    }

    // Обновляем в базе строку по заданному ID, добавляя количество дней,
    // потребление и среднее потребление за эти дни.
    private static void update(MeterDBHelper helper, int id, long span, double consumed, double average) {
        contentValues.clear();
        contentValues.put(ElectricMeterTable.COLUMN_SPAN, span);
        contentValues.put(ElectricMeterTable.COLUMN_CONSUMED, consumed);
        contentValues.put(ElectricMeterTable.COLUMN_AVERAGE, average);
        helper.getWritableDatabase().update(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                contentValues,
                ElectricMeterTable.COLUMN_ID + " = ?",
                new String[]{id + ""}
        );
    }

    // Обновление строки по заданному ID новыми значениями даты и показаний.
    private static void update(MeterDBHelper helper, int id, String date, int meter_reading) {
        contentValues.clear();
        contentValues.put(ElectricMeterTable.COLUMN_DATE, date);
        contentValues.put(ElectricMeterTable.COLUMN_READINGS, meter_reading);
        helper.getWritableDatabase().update(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                contentValues,
                ElectricMeterTable.COLUMN_ID + " = ?",
                new String[]{id + ""}

        );

    }

    // Обновление строки по заданному ID новыми значениями количества дней, даты,
    // показаний, потребления и среднего потребления.
    private static void update(MeterDBHelper helper, int id, long day, String date, int meter_reading, double potrebleno, double sred_potreb) {
        contentValues.clear();
        contentValues.put(ElectricMeterTable.COLUMN_SPAN, day);
        contentValues.put(ElectricMeterTable.COLUMN_DATE, date);
        contentValues.put(ElectricMeterTable.COLUMN_READINGS, meter_reading);
        contentValues.put(ElectricMeterTable.COLUMN_CONSUMED, potrebleno);
        contentValues.put(ElectricMeterTable.COLUMN_AVERAGE, sred_potreb);
        helper.getWritableDatabase().update(
                ElectricMeterTable.TABLE_ELECTRIC,
                contentValues,
                ElectricMeterTable.COLUMN_ID + " = ? ",
                new String[]{id + ""}
        );
    }

    // Создаём массив данных для графика.
    public static DataPoint[] getGraph(Context context) {
        MeterDBHelper helper = new MeterDBHelper(context);
        Cursor cursor = helper.getReadableDatabase().query(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                new String[]{ElectricMeterTable.COLUMN_DATE, ElectricMeterTable.COLUMN_CONSUMED},
                null,
                null,
                null,
                null,
                ElectricMeterTable.COLUMN_DATE + " ASC ",
                null
        );
        DataPoint[] dataPoint = new DataPoint[cursor.getCount() - 1];
        int id = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Date date;
            if (cursor.getString(1) != null) {
                double num = Double.parseDouble(cursor.getString(1));
                try {
                    date = format.parse(cursor.getString(0).trim());
                    if (date != null) {
                        dataPoint[id] = new DataPoint(date.getTime(), num);
                        ++id;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        helper.close();
        return dataPoint;
    }

    public static boolean proverkaDat(Context context, String data) {
        MeterDBHelper helper = new MeterDBHelper(context);
        Cursor cursor = helper.getReadableDatabase().query(
                ElectricMeterTable.TABLE_ELECTRIC + "",
                null,
                ElectricMeterTable.COLUMN_DATE + " = '" + data + "' ",
                null,
                null,
                null,
                ElectricMeterTable.COLUMN_DATE + " DESC ",
                " 1 "
        );
        boolean result = cursor.moveToFirst();
        cursor.close();
        helper.close();
        return result;
    }
}
