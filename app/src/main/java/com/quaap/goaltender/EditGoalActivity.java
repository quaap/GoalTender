package com.quaap.goaltender;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditGoalActivity extends AppCompatActivity {

    private int goalid = -1;
    private ArrayAdapter goaltypeadapter;
    private ArrayAdapter goalperiodadapter;
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



        final GoalDB db = MainActivity.getDatabase();

        List<Goal> goals = db.getAllGoals(false);
        final Spinner goallist = (Spinner) findViewById(R.id.editgoal_goallist);

        List<String> goalnames = new ArrayList<>();
        for (Goal g : goals) {
            goalnames.add(g.getName());
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalnames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goallist.setAdapter(adapter);

        LinearLayout switchlayout = (LinearLayout) findViewById(R.id.editgoal_switchlayout);
        if (goalnames.size() == 0) {
            switchlayout.setVisibility(View.GONE);
        }

        Button switchgoal = (Button) findViewById(R.id.editgoal_switch_goal);
        switchgoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Goal g = db.getGoal(goallist.getSelectedItem().toString());
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
        goalid = intent.getIntExtra("goalid", goalid);

        {
            Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);

            List<String> goaltypes = new ArrayList<>();
            for (Goal.Type t : Goal.Type.values()) {
                goaltypes.add(t.name());
            }
            goaltypeadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goaltypes);
            goaltypeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            goaltype.setAdapter(goaltypeadapter);

            goaltype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Goal.Type gtype = Goal.Type.valueOf(adapterView.getSelectedItem().toString());
                    isboolgoal = gtype == Goal.Type.Checkbox;
                    setBoolGoal();

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        Spinner goalperiod = (Spinner) findViewById(R.id.goal_period);

        List<String> goalperiods = new ArrayList<>();
        for (Goal.Period t : Goal.Period.values()) {
            goalperiods.add(t.name());
        }
        goalperiodadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalperiods);
        goalperiodadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalperiod.setAdapter(goalperiodadapter);

        goalperiod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Goal.Period gperiod = Goal.Period.valueOf(adapterView.getSelectedItem().toString());

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

    private void pickdays() {
        Intent pickdays = new Intent(this, DaysPickerActivity.class);

        //TextView entry_date = (TextView) findViewById(R.id.entry_date);

        pickdays.putExtra("daysflags", goal_days_picked);

        this.startActivityForResult(pickdays, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            TextView goal_days = (TextView) findViewById(R.id.goal_days);

            goal_days_picked = data.getIntExtra("daysflags",0);
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
        GoalDB db = MainActivity.getDatabase();

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
            goaltype.setSelection(goaltypeadapter.getPosition(goal.getType().name()));
            goalperiod.setSelection(goalperiodadapter.getPosition(goal.getPeriod().name()));

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
                .setTitle("Deleting Goal")
                .setMessage("This will delete all entries for this goal. Are you sure you want to delete this goal?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GoalDB db = MainActivity.getDatabase();
                        db.deleteGoal(goalid);
                        Intent output = new Intent();
                        output.putExtra("action", "deleted");
                        setResult(RESULT_OK, output);
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void save() {
        Intent output = new Intent();

        EditText goalname = (EditText) findViewById(R.id.editgoal_goalname);
        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);
        Spinner goalperiod = (Spinner) findViewById(R.id.goal_period);
        EditText goalnum = (EditText) findViewById(R.id.editgoal_goalnum);

        if (goalname.getText().toString().trim().length()==0) {
            Toast.makeText(this, "Please enter a goalname", Toast.LENGTH_SHORT).show();
            goalname.requestFocus();
            return;
        }

        if (goalnum.getVisibility()==View.VISIBLE && (goalnum.getText().toString().trim().length()==0 || !goalnum.getText().toString().matches("^-?[0-9]+(\\.[0-9]+)?$"))) {
            Toast.makeText(this, "Please enter a valid target value (number)", Toast.LENGTH_SHORT).show();
            goalnum.requestFocus();
            return;
        }

        AutoCompleteTextView editgoal_units = (AutoCompleteTextView) findViewById(R.id.editgoal_units);
        CheckBox ismax = (CheckBox) findViewById(R.id.editgoal_ismax);
        Switch active = (Switch) findViewById(R.id.goal_active_switch);

        GoalDB db = MainActivity.getDatabase();

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