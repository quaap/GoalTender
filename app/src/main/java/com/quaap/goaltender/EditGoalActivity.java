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


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Date;
import java.util.List;


public class EditGoalActivity extends AppCompatActivity {

    public static final String PASSINGGOALID = "goalid";
    private int goalid = -1;
    private ArrayAdapter<Goal.Type> goaltypeadapter;
    private ArrayAdapter<Goal.Period> goalperiodadapter;
    private boolean isboolgoal;

    private int goal_days_picked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_goal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button save = (Button) findViewById(R.id.editgoal_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        Button delete = (Button) findViewById(R.id.editgoal_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });
        delete.setVisibility(View.INVISIBLE);

        Button show_switch_goal = (Button) findViewById(R.id.goal_show_switch_goal);
        show_switch_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editgoal_switchlayout = findViewById(R.id.editgoal_switchlayout);
                editgoal_switchlayout.setVisibility(View.VISIBLE);
                view.setVisibility(View.INVISIBLE);
            }
        });



        final GoalDB db = GoalTender.getDatabase();

        List<Goal> goals = db.getAllGoals(false);
        final Spinner goallist = (Spinner) findViewById(R.id.editgoal_goallist);


        final ArrayAdapter<Goal> adapter = new ArrayAdapter<Goal>(this, android.R.layout.simple_spinner_item, goals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goallist.setAdapter(adapter);

        LinearLayout switchlayout = (LinearLayout) findViewById(R.id.editgoal_switchlayout);
        if (goals.size() == 0) {
            switchlayout.setVisibility(View.GONE);
        }

        Button switchgoal = (Button) findViewById(R.id.editgoal_switch_goal);
        switchgoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Goal g = (Goal)goallist.getSelectedItem();
                goalid = g.getId();
                loadGoal();
            }
        });

        final ArrayAdapter unitsadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, db.getAllUnits());
        unitsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        AutoCompleteTextView editgoal_units = (AutoCompleteTextView) findViewById(R.id.editgoal_units);
        editgoal_units.setAdapter(unitsadapter);
        editgoal_units.setThreshold(1);


        Intent intent = getIntent();
        goalid = intent.getIntExtra(PASSINGGOALID, goalid);


        setupGoaltypeSpinner();


        Spinner goalperiod = (Spinner) findViewById(R.id.goal_period);


        setupGoalperiodSpinner(goalperiod);

    }

    private void setupGoalperiodSpinner(Spinner goalperiod) {
        goalperiodadapter = Goal.Period.getArrayAdapter(this, android.R.layout.simple_spinner_item);
//        goalperiodadapter = new ArrayAdapter<Goal.Period>(this, android.R.layout.simple_spinner_item, Goal.Period.values());
//        goalperiodadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalperiod.setAdapter(goalperiodadapter);

        goalperiod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Goal.Period gperiod = (Goal.Period)adapterView.getSelectedItem();

                TextView goal_days = (TextView) findViewById(R.id.goal_days);
                if (gperiod == Goal.Period.NamedDays) {
                    goal_days.setVisibility(View.VISIBLE);
                    if (goal_days_picked == 0) {
                        goal_days.setText(R.string.setgoaldays);
                    }
                    goal_days.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            pickdays();
                        }
                    });

                } else {
                    goal_days.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupGoaltypeSpinner() {
        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);

        goaltypeadapter = Goal.Type.getArrayAdapter(this, android.R.layout.simple_spinner_item);
        //goaltypeadapter = new ArrayAdapter<Goal.Type>(this, android.R.layout.simple_spinner_item, Goal.Type.values());
        //goaltypeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goaltype.setAdapter(goaltypeadapter);

        goaltype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Goal.Type gtype = (Goal.Type)adapterView.getSelectedItem();
                isboolgoal = gtype == Goal.Type.Checkbox;
                setBoolGoal();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void pickdays() {
        Intent pickdays = new Intent(this, DaysPickerActivity.class);

        //TextView entry_date = (TextView) findViewById(R.id.entry_date);

        pickdays.putExtra(DaysPickerActivity.PASSINGDAYSFLAGS, goal_days_picked);

        this.startActivityForResult(pickdays, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            TextView goal_days = (TextView) findViewById(R.id.goal_days);

            goal_days_picked = data.getIntExtra(DaysPickerActivity.PASSINGDAYSFLAGS,0);
            goal_days.setText(TextUtils.join(", ", Goal.Days.split(goal_days_picked)));

        }
    }

    private void setBoolGoal() {

        int[] hideids = {R.id.textView5,
                R.id.editgoal_goalnum,
                R.id.textView9,
                R.id.editgoal_units,
                R.id.editgoal_ismax};

            for(int id: hideids) {
                View v = findViewById(id);
                v.setVisibility(isboolgoal?View.GONE:View.VISIBLE);
            }

    }

    private void loadGoal() {
        GoalDB db = GoalTender.getDatabase();

        EditText goalname = (EditText) findViewById(R.id.editgoal_goalname);

        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);

        Spinner goalperiod = (Spinner) findViewById(R.id.goal_period);

        EditText goalnum = (EditText) findViewById(R.id.editgoal_goalnum);

        CheckBox ismax = (CheckBox) findViewById(R.id.editgoal_ismax);

        AutoCompleteTextView editgoal_units = (AutoCompleteTextView) findViewById(R.id.editgoal_units);

        TextView goal_days = (TextView)findViewById(R.id.goal_days);

        Switch active = (Switch) findViewById(R.id.goal_active_switch);
        Button delete = (Button) findViewById(R.id.editgoal_delete);

        Goal goal = db.getGoal(goalid);

        if (goal != null) {
            goalname.setText(goal.getName());
            goaltype.setSelection(goaltypeadapter.getPosition(goal.getType()));
            goalperiod.setSelection(goalperiodadapter.getPosition(goal.getPeriod()));

            goalnum.setText(goal.getGoalnum() + "");
            editgoal_units.setText(goal.getUnits());
            ismax.setChecked(goal.getMinmax() == Goal.MinMax.Maximum);
            active.setChecked(goal.isActive());
            delete.setVisibility(View.VISIBLE);

            goal_days.setText(TextUtils.join(", ", goal.getDays()));
            goal_days_picked = Goal.Days.combine(goal.getDays());

        }
    }

    private void delete() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.deleting_goal)
                .setMessage(R.string.sure_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GoalDB db = GoalTender.getDatabase();
                        db.deleteGoal(goalid);
                        Intent output = new Intent();
                        output.putExtra("action", "deleted");
                        setResult(RESULT_OK, output);
                        finish();
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void save() {
        Intent output = new Intent();

        EditText goalname = (EditText) findViewById(R.id.editgoal_goalname);
        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);
        Spinner goalperiod = (Spinner) findViewById(R.id.goal_period);
        EditText goalnum = (EditText) findViewById(R.id.editgoal_goalnum);

        if (goalname.getText().toString().trim().length()==0) {
            Toast.makeText(this, R.string.enter_a_goalname, Toast.LENGTH_SHORT).show();
            goalname.requestFocus();
            return;
        }

        if (goalnum.getVisibility()==View.VISIBLE && (goalnum.getText().toString().trim().length()==0 || !goalnum.getText().toString().matches("^-?[0-9]+(\\.[0-9]+)?$"))) {
            Toast.makeText(this, R.string.enter_a_target, Toast.LENGTH_SHORT).show();
            goalnum.requestFocus();
            return;
        }

        AutoCompleteTextView editgoal_units = (AutoCompleteTextView) findViewById(R.id.editgoal_units);
        CheckBox ismax = (CheckBox) findViewById(R.id.editgoal_ismax);
        Switch active = (Switch) findViewById(R.id.goal_active_switch);

        GoalDB db = GoalTender.getDatabase();

        Goal goal;
        if (goalid > -1) {
            goal = db.getGoal(goalid);
        } else {
            goal = db.getGoal(goalname.getText().toString());
        }
        if (goal == null) {
            goal = new Goal();
        }

        goal.setName(goalname.getText().toString());
        goal.setType(Goal.Type.valueOf(goaltype.getSelectedItem().toString()));
        goal.setPeriod(Goal.Period.valueOf(goalperiod.getSelectedItem().toString()));


        if (goal.getStartDate() == null) {
            goal.setStartDate(new Date());
        }

        if (isboolgoal) {
            goal.setGoalnum(1);
        } else {
            goal.setGoalnum(Float.parseFloat(goalnum.getText().toString()));
            goal.setUnits(editgoal_units.getText().toString());
        }
        goal.setMinmax(ismax.isChecked() ? Goal.MinMax.Maximum : Goal.MinMax.Minimum);

        goal.setActive(active.isChecked());

        goal.setDays(goal_days_picked);

        db.addGoal(goal);
        output.putExtra("action", "saved");

        setResult(RESULT_OK, output);
        finish();
    }
}