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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.quaap.goaltender.storage.Goal;


public class DaysPickerActivity extends AppCompatActivity {

    public static final String PASSINGDAYSFLAGS = "daysflags";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_days_picker);
        Intent intent = getIntent();
        int daysflags = intent.getIntExtra(PASSINGDAYSFLAGS, 0);

        Button done = (Button)findViewById(R.id.pick_days_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        CheckBox check_sun = (CheckBox)findViewById(R.id.check_sun);
        CheckBox check_mon = (CheckBox)findViewById(R.id.check_mon);
        CheckBox check_tue = (CheckBox)findViewById(R.id.check_tue);
        CheckBox check_wed = (CheckBox)findViewById(R.id.check_wed);
        CheckBox check_thu = (CheckBox)findViewById(R.id.check_thu);
        CheckBox check_fri = (CheckBox)findViewById(R.id.check_fri);
        CheckBox check_sat = (CheckBox)findViewById(R.id.check_sat);

        check_sun.setChecked(Goal.Days.contains(daysflags, Goal.Days.Sunday));
        check_mon.setChecked(Goal.Days.contains(daysflags, Goal.Days.Monday));
        check_tue.setChecked(Goal.Days.contains(daysflags, Goal.Days.Tuesday));
        check_wed.setChecked(Goal.Days.contains(daysflags, Goal.Days.Wednesday));
        check_thu.setChecked(Goal.Days.contains(daysflags, Goal.Days.Thursday));
        check_fri.setChecked(Goal.Days.contains(daysflags, Goal.Days.Friday));
        check_sat.setChecked(Goal.Days.contains(daysflags, Goal.Days.Saturday));

    }

    private void save() {
        CheckBox check_sun = (CheckBox)findViewById(R.id.check_sun);
        CheckBox check_mon = (CheckBox)findViewById(R.id.check_mon);
        CheckBox check_tue = (CheckBox)findViewById(R.id.check_tue);
        CheckBox check_wed = (CheckBox)findViewById(R.id.check_wed);
        CheckBox check_thu = (CheckBox)findViewById(R.id.check_thu);
        CheckBox check_fri = (CheckBox)findViewById(R.id.check_fri);
        CheckBox check_sat = (CheckBox)findViewById(R.id.check_sat);

        int daysflags = 0;
        if (check_sun.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Sunday);
        if (check_mon.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Monday);
        if (check_tue.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Tuesday);
        if (check_wed.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Wednesday);
        if (check_thu.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Tuesday);
        if (check_fri.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Friday);
        if (check_sat.isChecked()) daysflags = Goal.Days.add(daysflags, Goal.Days.Saturday);

        Intent output = new Intent();
        output.putExtra(PASSINGDAYSFLAGS, daysflags);

        setResult(RESULT_OK, output);
        finish();
    }
}
