package com.example.meter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private TextView textView;
    private FloatingActionButton floatingActionButton;
    private Button colorBottom;
    private int num = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.colorText);
        floatingActionButton = findViewById(R.id.floating);
        colorBottom = findViewById(R.id.colorBottom);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

//        GraphView graph = (GraphView) findViewById(R.id.graph);
//        DataPoint[] dataPoint = DBWork.getGraph(this);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoint);
//        graph.getViewport().setScrollable(true); // enables horizontal scrolling
//        graph.getViewport().setScrollableY(true); // enables vertical scrolling
//        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
//        graph.getViewport().setScalableY(true);
//        graph.addSeries(series);
//        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
//        graph.getViewport().setMinX(dataPoint[0].getX());
//        graph.getViewport().setMaxX(dataPoint[dataPoint.length-1].getX());
//        graph.getViewport().setXAxisBoundsManual(true);
//        graph.getGridLabelRenderer().setHumanRounding(false);

    }


    public void resultView(View view) {
        Intent intent = new Intent(this, ElectricResult.class);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.electric) {
            floatingActionButton.setBackgroundColor(getResources().getColor(R.color.electric));
            colorBottom.setBackgroundColor(getResources().getColor(R.color.electric));
            textView.setBackgroundColor(getResources().getColor(R.color.electric));
            textView.setText("Electric");
            num = 1;
            return true;
        }
        if (item.getItemId() == R.id.gaz) {
            floatingActionButton.setBackgroundColor(getResources().getColor(R.color.gaz));
            colorBottom.setBackgroundColor(getResources().getColor(R.color.gaz));
            textView.setBackgroundColor(getResources().getColor(R.color.gaz));
            textView.setText("gaz");
            num = 2;
            return true;
        }
        if (item.getItemId() == R.id.water) {
            floatingActionButton.setBackgroundColor(getResources().getColor(R.color.water));
            colorBottom.setBackgroundColor(getResources().getColor(R.color.water));
            textView.setBackgroundColor(getResources().getColor(R.color.water));
            textView.setText("water");
            num = 3;
            return true;
        }
        return false;
    }

    public void floatClick(View view) {
        if (num == 1) showDialog();
        else Toast.makeText(this, "Здесь ещё ничего нет", Toast.LENGTH_LONG).show();
    }

    private void showDialog() {
        View dialog = getLayoutInflater().inflate(R.layout.add_to_base, null);
        EditText pokazaniya = dialog.findViewById(R.id.ediText);
        TextView dateText = dialog.findViewById(R.id.dateText);
        Button cancel = dialog.findViewById(R.id.dialog_button_cancel);
        Button accept = dialog.findViewById(R.id.dialog_button_accept);

        dateText.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(new Date()));
        dateText.setOnClickListener(v -> {
            String[] date = dateText.getText().toString().split("\\.");
            int mYear = Integer.parseInt(date[2]);
            int mMonth = Integer.parseInt(date[1]) - 1;
            int mDay = Integer.parseInt(date[0]);
            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String datepickertext;
                        if (dayOfMonth < 10) datepickertext = "0" + dayOfMonth + ".";
                        else datepickertext = dayOfMonth + ".";
                        if ((month + 1) < 10)
                            datepickertext = datepickertext + "0" + (month + 1) + "." + year;
                        else datepickertext = datepickertext + (month + 1) + "." + year;
                        dateText.setText(datepickertext);
                        String data_v_baze = DBWork.getDate(dateText);
                        if (DBWork.proverkaDat(this, data_v_baze)) {
                            dialog.findViewById(R.id.error_date).setVisibility(View.VISIBLE);
                            accept.setClickable(false);

                        } else {
                            dialog.findViewById(R.id.error_date).setVisibility(View.INVISIBLE);
                            accept.setClickable(true);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialog);
        AlertDialog dial = builder.create();
        dial.show();
        dial.getWindow().setBackgroundDrawableResource(R.color.grey);
        if(pokazaniya.length()<1){
            accept.setActivated(false);
            accept.setTextColor(getResources().getColor(R.color.grey));
        }
        if (dialog.findViewById(R.id.error_date).getVisibility() == View.VISIBLE) {
            accept.setActivated(false);
            accept.setTextColor(getResources().getColor(R.color.grey));
        } else {
            accept.setActivated(true);
            accept.setTextColor(getResources().getColor(R.color.white));
        }
        cancel.setOnClickListener(v -> dial.dismiss());
        accept.setOnClickListener(v -> {
            Log.d("amicus", pokazaniya.length()+" ."+pokazaniya.getText()+".");
            String dat = DBWork.getDate(dateText);
            int meter_reading = Integer.parseInt(pokazaniya.getText().toString());
            DBWork.miMax(v.getContext(), dat, meter_reading);
            dial.dismiss();
        });
    }
}