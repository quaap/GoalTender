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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;


import java.util.Date;
import java.util.List;


public class EditEntryActivity extends AppCompatActivity {

    public static final String PASSINGENTRYID = "entry_id";
    public static final String PASSINGGOALID = "goal_id";
    private int entry_id = -1;
    private Entry entry = null;
    private Date date;

    private ArrayAdapter<Goal> goaladapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        Button save = (Button) findViewById(R.id.entry_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        Button delete = (Button) findViewById(R.id.entry_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });


        GoalDB db = GoalTender.getDatabase();

        List<Goal> goals = db.getAllGoals(true);
        Spinner goalid = (Spinner) findViewById(R.id.entry_goalid);


        goaladapter = Goal.getArrayAdapter(this, android.R.layout.simple_spinner_item, goals);
        goalid.setAdapter(goaladapter);

        goalid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                goalChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });

        TextView entry_date = (TextView) findViewById(R.id.entry_date);
        final EditText entry_value = (EditText) findViewById(R.id.entry_value);
        CheckBox bool_goal_complete = (CheckBox) findViewById(R.id.bool_goal_complete);
        final EditText entry_comment = (EditText) findViewById(R.id.entry_comment);
        final TextView entry_comment_lab = (TextView) findViewById(R.id.entry_comment_lab);
        TextView entry_units = (TextView) findViewById(R.id.editentry_units);
        ImageButton showcomm = (ImageButton) findViewById(R.id.entry_showcomment);

        showcomm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (entry_comment.getVisibility()==View.VISIBLE) {
                    entry_comment.setVisibility(View.GONE);
                    entry_comment_lab.setVisibility(View.GONE);
                } else {
                    entry_comment.setVisibility(View.VISIBLE);
                    entry_comment_lab.setVisibility(View.VISIBLE);
                }

            }
        });

        entry_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickdatetime();
            }
        });


        Intent intent = getIntent();
        entry_id = intent.getIntExtra(PASSINGENTRYID, -1);

        //System.out.println("Entryid: " + entry_id);

        if (entry_id >= 0) {
            entry = db.getEntry(entry_id);

            goalid.setSelection(goaladapter.getPosition(entry.getGoal()));

            date = entry.getDate();

            if (entry.getGoal().getType() == Goal.Type.Checkbox) {
                bool_goal_complete.setChecked(entry.getValue()>0);
            } else {
                entry_value.setText(entry.getValue() + "");
                entry_units.setText(entry.getGoal().getUnits());
            }
            String comm = entry.getComment();
            if (comm!=null && comm.length()>0) {
                entry_comment.setText(comm);
                entry_comment.setVisibility(View.VISIBLE);
                entry_comment_lab.setVisibility(View.VISIBLE);
            }
        } else {
            delete.setVisibility(View.GONE);
            int goal_id = intent.getIntExtra(PASSINGGOALID, -1);
            if (goal_id >= 0) {
                goalid.setSelection(goaladapter.getPosition(db.getGoal(goal_id)));
            }

            date = new Date();
        }
        entry_date.setText(Utils.showDate(date));
        goalChanged();

        entry_value.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(entry_value, 0);
            }
        }, 200);
    }

    private void goalChanged() {
        Spinner goalid = (Spinner) findViewById(R.id.entry_goalid);

        Goal g = (Goal)goalid.getSelectedItem();
        TextView entry_units = (TextView) findViewById(R.id.editentry_units);

        entry_units.setText(g.getUnits());


        final EditText entry_value = (EditText) findViewById(R.id.entry_value);
        CheckBox bool_goal_complete = (CheckBox) findViewById(R.id.bool_goal_complete);
        View value_label = findViewById(R.id.value_label);


        if (g.getType() == Goal.Type.Checkbox) {
            bool_goal_complete.setVisibility(View.VISIBLE);
            entry_value.setVisibility(View.GONE);
            entry_units.setVisibility(View.GONE);
            value_label.setVisibility(View.GONE);
        } else {
            bool_goal_complete.setVisibility(View.GONE);
            entry_value.setVisibility(View.VISIBLE);
            entry_units.setVisibility(View.VISIBLE);
            value_label.setVisibility(View.VISIBLE);
        }
    }

    private void pickdatetime() {
        Intent pickdatetime = new Intent(this, PickDateTimeActivity.class);


        if (date!=null) {
            pickdatetime.putExtra("date", date.getTime());
        } else if (entry != null) {
            pickdatetime.putExtra("date", entry.getDate().getTime());
        }

        this.startActivityForResult(pickdatetime, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            TextView entry_date = (TextView) findViewById(R.id.entry_date);

            date = Utils.stringToDate(data.getStringExtra("date"));
            entry_date.setText(Utils.showDate(date));


        }
    }

    private void delete() {


        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GoalDB db = GoalTender.getDatabase();
                        db.deleteEntry(entry_id);
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

        Spinner goalid = (Spinner) findViewById(R.id.entry_goalid);
        CheckBox bool_goal_complete = (CheckBox) findViewById(R.id.bool_goal_complete);
        EditText entry_value = (EditText) findViewById(R.id.entry_value);

        if (entry_value.getVisibility()==View.VISIBLE && (entry_value.getText().toString().trim().length()==0 || !entry_value.getText().toString().matches("^-?[0-9]+(\\.[0-9]+)?$"))) {
            Toast.makeText(this, "Please enter a valid value (number)", Toast.LENGTH_SHORT).show();
            entry_value.requestFocus();
            return;
        }

        EditText entry_comment = (EditText) findViewById(R.id.entry_comment);

        GoalDB db = GoalTender.getDatabase();



        if (entry==null) entry = new Entry();
        Goal g = db.getGoal(goalid.getSelectedItem().toString());
        entry.setGoal(g);
        entry.setDate(date);
        if (g.getType() == Goal.Type.Checkbox) {
            entry.setValue(bool_goal_complete.isChecked()?1:0);
        } else {
            try {
                entry.setValue(Float.parseFloat(entry_value.getText().toString()));
            } catch(java.lang.NumberFormatException e) {
                entry.setValue(0);
            }
        }
        entry.setComment(entry_comment.getText().toString());

        db.addEntry(entry);

        output.putExtra("action", "saved");
        setResult(RESULT_OK, output);
        finish();
    }
}
