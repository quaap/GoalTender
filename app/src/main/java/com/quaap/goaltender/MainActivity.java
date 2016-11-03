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
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EntryItemArrayAdapter listitemadapter;

    private Goal currentGoal = null;

    private GoalDB db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = GoalTender.getDatabase();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFab();
            }
        });

        populateList();

        ListView mainList = (ListView) findViewById(R.id.mainList);
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEntryEditor(id, position);
            }
        });

    }

    private void handleFab() {
        if (currentGoal==null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.showOverflowMenu();
        } else {
            showEntryEditor(currentGoal.getId());
        }
    }


    private void populateList() {
        populateList(null);
    }
    private void populateList(int goalid) {
        populateList(db.getGoal(goalid));
    }


    private void populateList(Goal g) {
        currentGoal = g;
        List<String> listitems = new ArrayList<>();

        TextView entries_list_title = (TextView)findViewById(R.id.entries_list_title);
        List<Entry> listentry;
        if (g == null) {
            listentry = db.getUnmetEntries();
            listentry.addAll(db.getAllEntriesCollapsed());
            entries_list_title.setText(R.string.list_all_entries);
        } else {
            listentry = db.getAllEntries(g);
            if (listentry.size()==0) {
                Toast.makeText(this, getString(R.string.list_no_entries) + g.getName(), Toast.LENGTH_SHORT).show();
                return;
            }
            entries_list_title.setText(getString(R.string.list_entries_goal) + g.getName());
        }
        for (Entry entry : listentry) {
            listitems.add(entry.getGoal().getName() + " " + entry.getDate().toString());
        }
        ListView mainList = (ListView) findViewById(R.id.mainList);

        listitemadapter = new EntryItemArrayAdapter(this, listitems.toArray(new String[0]), listentry);
        listitemadapter.setMoreGoalClick(new EntryItemArrayAdapter.OnMoreGoalClick() {
            @Override
            public void itemClicked(int goalid) {
                populateList(goalid);
            }
        });
        listitemadapter.setGoallist(g!=null);
        mainList.setAdapter(listitemadapter);

    }

    @Override
    public void onBackPressed() {
        if (currentGoal != null) {
            populateList();
        } else {

            super.onBackPressed();
        }
    }

    private int entry_edit_code = 1;

    private void showEntryEditor(long id, int pos) {

        Entry entry = null;
        if (!listitemadapter.isEmpty() && pos >= 0) entry = listitemadapter.getEntry(pos);

        if (entry != null && entry.isCollapsed()) {
            populateList(entry.getGoal());
        } else {
            Intent entry_edit = new Intent(this, EditEntryActivity.class);

            entry_edit.putExtra("entry_id", (int) id);
            if (entry!=null) {
                entry_edit.putExtra("goal_id", entry.getGoal().getId());
            }

            this.startActivityForResult(entry_edit, entry_edit_code);
        }
    }

    private void showEntryEditor(int goalid) {


        Intent entry_edit = new Intent(this, EditEntryActivity.class);

        entry_edit.putExtra("goal_id", (int) goalid);

        this.startActivityForResult(entry_edit, entry_edit_code);

    }


    private final int goal_edit_code = 2;

    private void showGoalEditor(long id) {
        Intent goal_edit = new Intent(this, EditGoalActivity.class);

        //goal_edit.putExtra("goal_id", (int)id);

        this.startActivityForResult(goal_edit, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String action = null;
        if (data!=null) {
            action = data.getStringExtra("action");
        }

        if (action == null) action = "somethinged";

        if (requestCode == entry_edit_code && resultCode == RESULT_OK) {
            Toast.makeText(this, "Entry " + action, Toast.LENGTH_SHORT).show();
        } else if (requestCode == goal_edit_code && resultCode == RESULT_OK) {
            Toast.makeText(this, "Goal " + action, Toast.LENGTH_SHORT).show();
        }
        populateList();
        invalidateOptionsMenu();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        for (Goal g : db.getAllGoals(true)) {
            if (menu.findItem(1001 + g.getId()) == null) {
                menu.add(Menu.NONE, 1001 + g.getId(), Menu.NONE, "New " + g.getName() + " Entry");
            }

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showGoalEditor(-1);
            return true;
        } else if (id == R.id.export_csv) {

            db.export();
        } else {
            showEntryEditor(id - 1001);
        }

        return super.onOptionsItemSelected(item);
    }


}
