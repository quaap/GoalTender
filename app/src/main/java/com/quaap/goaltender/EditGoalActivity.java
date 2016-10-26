package com.quaap.goaltender;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditGoalActivity extends AppCompatActivity {

    int goalid = -1;
    ArrayAdapter goaltypeadapter;

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
        Button delete = (Button) findViewById(R.id.editgoal_archive);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete();
            }
        });




        final GoalDB db = MainActivity.getDatabase();

        List<Goal> goals = db.getAllGoals(true);
        final Spinner goallist = (Spinner) findViewById(R.id.editgoal_goallist);

        List<String> goalnames = new ArrayList<>();
        for (Goal g: goals) {
            goalnames.add(g.getName());
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalnames);
        goallist.setAdapter(adapter);

        LinearLayout switchlayout = (LinearLayout) findViewById(R.id.editgoal_switchlayout);
        if (goalnames.size()==0) {
            switchlayout.setVisibility(View.INVISIBLE);
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
        AutoCompleteTextView editgoal_units = (AutoCompleteTextView)findViewById(R.id.editgoal_units);
        editgoal_units.setAdapter(unitsadapter);
        editgoal_units.setThreshold(1);



        Intent intent = getIntent();
        goalid = intent.getIntExtra("goalid", goalid);


        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);

        List<String> goaltypes = new ArrayList<>();
        for (Goal.Type t : Goal.Type.values()) {
            goaltypes.add(t.name());
        }
        goaltypeadapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goaltypes);
        goaltype.setAdapter(goaltypeadapter);



    }

    private void loadGoal() {
        GoalDB db = MainActivity.getDatabase();

        EditText goalname = (EditText) findViewById(R.id.editgoal_goalname);

        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);

        EditText goalnum = (EditText) findViewById(R.id.editgoal_goalnum);

        AutoCompleteTextView editgoal_units = (AutoCompleteTextView)findViewById(R.id.editgoal_units);

        Goal goal = db.getGoal(goalid);

        if (goal != null) {
            goalname.setText(goal.getName());
            goaltype.setSelection(goaltypeadapter.getPosition(goal.getType().name()));
            goalnum.setText(goal.getGoalnum() + "");
            editgoal_units.setText(goal.getUnits());

        }
    }

    private void save() {
        Intent output = new Intent();

        EditText goalname = (EditText) findViewById(R.id.editgoal_goalname);
        Spinner goaltype = (Spinner) findViewById(R.id.editgoal_type);
        EditText goalnum = (EditText) findViewById(R.id.editgoal_goalnum);
        AutoCompleteTextView editgoal_units = (AutoCompleteTextView)findViewById(R.id.editgoal_units);

        GoalDB db = MainActivity.getDatabase();

        Goal goal;
        if (goalid>-1) {
            goal = db.getGoal(goalid);
        } else {
            goal = db.getGoal(goalname.getText().toString());
        }
        if (goal == null) {
            goal = new Goal();
        }

        goal.setName(goalname.getText().toString());
        goal.setType(Goal.Type.valueOf(goaltype.getSelectedItem().toString()));
        if (goal.getStartDate()==null) {
            goal.setStartDate(new Date());
        }

        goal.setGoalnum(Float.parseFloat(goalnum.getText().toString()));

        goal.setUnits(editgoal_units.getText().toString());

        db.addGoal(goal);

        setResult(RESULT_OK, output);
        finish();
    }
}