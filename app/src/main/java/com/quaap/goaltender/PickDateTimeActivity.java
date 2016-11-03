package com.quaap.goaltender;

/**
 *   Copyright 2016 Tom Kliethermes
 *
 *   This file is part of GoalTender.
 *
 *   GoalTender is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GoalTender is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GoalTender.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.quaap.goaltender.storage.GoalDB;

import java.util.Calendar;
import java.util.Date;

public class PickDateTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_date_time);

        DatePicker datep = (DatePicker) findViewById(R.id.datePicker);
        //TimePicker timep = (TimePicker) findViewById(R.id.timePicker);

        Intent intent = getIntent();
        long datelong = intent.getLongExtra("date", new Date().getTime());

        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(datelong);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datep.init(year, month, day, null);

//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//        int minute = cal.get(Calendar.MINUTE);
//        //int second = intent.getIntExtra("second", cal.get(Calendar.SECOND));
//
//        timep.setCurrentHour(hour);
//        timep.setCurrentMinute(minute);

        Button pick_datetime = (Button) findViewById(R.id.datetime_save);
        pick_datetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    private void save() {
        Intent output = new Intent();
        DatePicker datep = (DatePicker) findViewById(R.id.datePicker);
      //  TimePicker timep = (TimePicker) findViewById(R.id.timePicker);



//        datep.setO
//
//                ;setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
//                DatePicker datep = (DatePicker)view;
//                int year = datep.getYear();
//                int month = datep.getMonth();
//                int day = datep.getDayOfMonth();
//                TextView date = (TextView)findViewById(R.id.datepicker_date);
//                date.setText(datep.);
//            }
//        });

        int year = datep.getYear();
        int month = datep.getMonth();
        int day = datep.getDayOfMonth();

       // int hour = timep.getCurrentHour();
      //  int minute = timep.getCurrentMinute();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        output.putExtra("date", Utils.dateToString(new Date(cal.getTimeInMillis())));

        setResult(RESULT_OK, output);
        finish();
    }
}
