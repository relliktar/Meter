package com.example.meter;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ElectricResult extends AppCompatActivity {
    private MeterDBHelper helper;
    private SimpleCursorAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_list);
        helper = new MeterDBHelper(this);
        ListView listView = findViewById(R.id.info_list);
        listView.setEmptyView(findViewById(R.id.empty));
        registerForContextMenu(listView);
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_meter,
                null,
                new String[]{ElectricMeterTable.COLUMN_SPAN,
                        ElectricMeterTable.COLUMN_DATE,
                        ElectricMeterTable.COLUMN_READINGS,
                        ElectricMeterTable.COLUMN_CONSUMED,
                        ElectricMeterTable.COLUMN_AVERAGE},
                new int[]{R.id.day_pass,
                        R.id.date_item,
                        R.id.readable_item,
                        R.id.consumed,
                        R.id.mean},
                0
        );
        listView.setAdapter(adapter);
        updateCursor();
    }

    private void updateCursor() {
        Cursor cursor = helper.getReadableDatabase().query(
                ElectricMeterTable.TABLE_ELECTRIC,
                null,
                null,
                null,
                null,
                null,
                ElectricMeterTable.COLUMN_DATE + " DESC "
        );
        adapter.swapCursor(cursor);
    }

    public void clearBase(View view) {
        helper.getWritableDatabase().delete(
                ElectricMeterTable.TABLE_ELECTRIC,
                null,
                null
        );
        updateCursor();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.context_edit:
                updateValue(info.position);
                updateCursor();
                return true;
            case R.id.context_delete:
                deleteValue(info.position);
                updateCursor();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteValue(int position) {
        // Получаем курсор из адаптера
        Cursor cursor = adapter.getCursor();
        // Двигаем курсор на выбранную позицию (строка, на которой было вызвано меню)
        cursor.moveToPosition(position);
        // Получаем из курсора ID строки в базе данных.
        String databaseId = cursor.getString(cursor.getColumnIndex(ElectricMeterTable.COLUMN_ID));
        // Удаляем из базы строку по выбраному ID
        helper.getWritableDatabase().delete(
                ElectricMeterTable.TABLE_ELECTRIC+"",
                ElectricMeterTable.COLUMN_ID +" = ? ",
                new String[]{databaseId}
        );
        cursor.close();
        updateCursor();
    }

    private void updateValue(int position) {
        // Создаём диалоговое окно с обновлением выбранной позиции.
        View dialog = getLayoutInflater().inflate(R.layout.add_to_base, null);
        // Получаем курсор из адаптера.
        Cursor cursor = adapter.getCursor();
        // Двигаем курсор на выбранную позицию (строка, на которой было вызвано меню)
        cursor.moveToPosition(position);
        // Получаем из курсора ID выбранной строки в базе данных,
        String databaseId = cursor.getString(cursor.getColumnIndex(ElectricMeterTable.COLUMN_ID));
        // значение показаний,
        String value = cursor.getString(cursor.getColumnIndex(ElectricMeterTable.COLUMN_READINGS));
        // значение даты.
        String dat = cursor.getString(cursor.getColumnIndex(ElectricMeterTable.COLUMN_DATE));

        EditText pokazaniya = dialog.findViewById(R.id.ediText);
        TextView dateText = dialog.findViewById(R.id.dateText);
        // Присваиваем значение показаний в поле показаний диалогового окна.
        pokazaniya.setText(value);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            // Форматируем дату для
            Date data = format.parse(dat);
            // Дата не должна быть нулём.
            assert data != null;
            // Присваиваем значение даты в поле даты диалогового окна.
            dateText.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(data));
            // Обрабатываем нажатие на поле даты диалогового окна.
            dateText.setOnClickListener(v -> {
                // Разбираем дату из диалога на день, месяц и год для передачи в диалог даты.
                String[] date = dateText.getText().toString().split("\\.");
                int mYear = Integer.parseInt(date[2]);
                int mMonth = Integer.parseInt(date[1]) - 1;
                int mDay = Integer.parseInt(date[0]);
                // Создаём диалог с вводом даты.
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        (view, year, month, dayOfMonth) -> {
                            String datepickertext;
                            if (dayOfMonth < 10) datepickertext = "0" + dayOfMonth + ".";
                            else datepickertext = dayOfMonth + ".";
                            if ((month + 1) < 10)
                                datepickertext = datepickertext + "0" + (month + 1) + "." + year;
                            else datepickertext = datepickertext + (month + 1) + "." + year;
                            // Устанавливаем выбранную дату.
                            dateText.setText(datepickertext);
                            // Проверяем наличие выбранной даты в базе.
                            String data_v_baze = DBWork.getDate(dateText);
                            // Если есть, то показываем предупреждение.
                            if (DBWork.proverkaDat(this, data_v_baze)){
                                dialog.findViewById(R.id.error_date).setVisibility(View.VISIBLE);
                            }
                            else dialog.findViewById(R.id.error_date).setVisibility(View.INVISIBLE);
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setView(dialog);
        b.setPositiveButton("Изменить", (dialog1, which) -> {
            if( dialog.findViewById(R.id.error_date).getVisibility() == View.INVISIBLE){
            String dt = DBWork.getDate(dateText);
            int meter_reading = Integer.parseInt(pokazaniya.getText().toString());
            // Передаём для обновления ID строки, новую дату, новые показания, старую дату)
            DBWork.updateLine(this, databaseId, dt, meter_reading, dat);}
            updateCursor();
        });
        b.setNegativeButton("Отмена", (dialog12, which) -> dialog12.cancel());
        b.create().show();
        cursor.close();
    }
}
