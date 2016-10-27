package com.quaap.goaltender;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        goalid.setAdapter(adapter);

        TextView entry_date = (TextView) findViewById(R.id.entry_date);
        EditText entry_value = (EditText) findViewById(R.id.entry_value);
        EditText entry_comment = (EditText) findViewById(R.id.entry_comment);


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
            entry_date.setText(GoalDB.formatDateTime(entry.getDate()));
            entry_value.setText(entry.getValue()+"");
            entry_comment.setText(entry.getComment());
        } else  {
            entry_date.setText(GoalDB.formatDateTime(new Date()));
        }
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

            entry_date.setText(GoalDB.formatDateTime(data.getLongExtra("date", new Date().getTime())));

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
        entry.setDate(GoalDB.getDateTime(entry_date.getText().toString()));
        entry.setValue(Float.parseFloat(entry_value.getText().toString()));
        entry.setComment(entry_comment.getText().toString());

        db.addEntry(entry);

        setResult(RESULT_OK, output);
        finish();
    }
}
