package com.quaap.goaltender;


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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EditEntryActivity extends AppCompatActivity {

    int entry_id = -1;
    Entry entry = null;
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


        GoalDB db = MainActivity.getDatabase();

        List<Goal> goals = db.getAllGoals(true);
        Spinner goalid = (Spinner) findViewById(R.id.entry_goalid);

        List<String> goalnames = new ArrayList<>();
        for (Goal g: goals) {
            goalnames.add(g.getName());
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalnames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goalid.setAdapter(adapter);

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
        EditText entry_comment = (EditText) findViewById(R.id.entry_comment);
        TextView entry_units = (TextView) findViewById(R.id.editentry_units);

        entry_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickdatetime();
            }
        });


        Intent intent = getIntent();
        entry_id = intent.getIntExtra("entry_id", -1);
        if (entry_id>=0) {
            entry = db.getEntry(entry_id);
            goalid.setSelection(adapter.getPosition(entry.getGoal().getName()));
            entry_date.setText(GoalDB.dateToString(entry.getDate()));
            entry_value.setText(entry.getValue()+"");
            entry_units.setText(entry.getGoal().getUnits());
            entry_comment.setText(entry.getComment());
        } else  {
            delete.setVisibility(View.INVISIBLE);
            int goal_id= intent.getIntExtra("goal_id", -1);
            if (goal_id>=0) {
                goalid.setSelection(adapter.getPosition(db.getGoal(goal_id).getName()));
            }
            entry_date.setText(GoalDB.dateToString(new Date()));
            goalChanged();
        }
        //goalid.requestFocus();
        //entry_value.requestFocus();

        entry_value.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(entry_value, 0);
            }
        },200);
    }

    private void goalChanged() {
        Spinner goalid = (Spinner) findViewById(R.id.entry_goalid);
        GoalDB db = MainActivity.getDatabase();
        Goal g = db.getGoal(goalid.getSelectedItem().toString());
        TextView entry_units = (TextView) findViewById(R.id.editentry_units);
        entry_units.setText(g.getUnits());
    }

    private void pickdatetime(){
        Intent pickdatetime = new Intent(this, PickDateTimeActivity.class);

        TextView entry_date = (TextView) findViewById(R.id.entry_date);

        if (entry!=null) {
            pickdatetime.putExtra("date", entry.getDate().getTime());
        }

        this.startActivityForResult(pickdatetime, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            TextView entry_date = (TextView) findViewById(R.id.entry_date);

            entry_date.setText(data.getStringExtra("date"));

        }
    }

    private void delete(){


        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GoalDB db = MainActivity.getDatabase();
                        db.deleteEntry(entry_id);
                        Intent output = new Intent();
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
        TextView entry_date = (TextView) findViewById(R.id.entry_date);
        EditText entry_value = (EditText) findViewById(R.id.entry_value);
        EditText entry_comment = (EditText) findViewById(R.id.entry_comment);

        GoalDB db = MainActivity.getDatabase();

        System.out.println(goalid.getSelectedItem().toString());
        System.out.println(goalid.getSelectedItemId());

        Entry entry = new Entry();
        Goal g = db.getGoal(goalid.getSelectedItem().toString());
        entry.setGoal(g);
        entry.setDate(GoalDB.stringToDate(entry_date.getText().toString()));
        entry.setValue(Float.parseFloat(entry_value.getText().toString()));
        entry.setComment(entry_comment.getText().toString());

        db.addEntry(entry);

        setResult(RESULT_OK, output);
        finish();
    }
}
