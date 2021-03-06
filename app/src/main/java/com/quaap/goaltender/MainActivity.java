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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quaap.goaltender.notify.NotifyService;
import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EntryItemArrayAdapter.EntryItemClickListener {

    private EntryItemArrayAdapter listitemadapter;

    private Goal currentGoal = null;

    private GoalDB db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoalTender.setRunning(true);

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

        killnotify();
    }

    public void killnotify() {
        Intent servintent = new Intent(this, NotifyService.class);
        servintent.putExtra(NotifyService.CMD, NotifyService.CMD_KILLNOTIFY);
        startService(servintent);
    }

    public void setalarm() {
        Intent servintent = new Intent(this, NotifyService.class);
        servintent.putExtra(NotifyService.CMD, NotifyService.CMD_SETALARM);
        startService(servintent);
    }

    @Override
    protected void onResume() {
        killnotify();
        GoalTender.setRunning(true);
        setalarm();
        super.onResume();
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        GoalTender.setRunning(false);
        super.onPause();
    }


    @Override
    protected void onStop() {
        GoalTender.setRunning(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        GoalTender.setRunning(false);
        super.onDestroy();
    }

    private void handleFab() {
        if (currentGoal==null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.showOverflowMenu();
        } else {
            showEntryEditorForGoal(currentGoal.getId());
        }
    }


    private void populateList() {
        populateList(null);
    }
    private void populateList(int goalid) {
        populateList(db.getGoal(goalid));
    }

    private int startindex = 0;
    private int lengthindex = 150;

    private enum Viewing {Unmet, All, Goal}

    Viewing viewing = Viewing.Unmet;
    int moreentries = 0;


    private void populateList(Goal g) {
        currentGoal = g;
        moreentries = 0;

        TextView entries_list_title = (TextView)findViewById(R.id.entries_list_title);
        List<Entry> listentry = null;
        String noitemtext = "";
        if (g == null) {

            if (viewing == Viewing.Unmet) {
                listentry = db.getUnmetEntries();
                entries_list_title.setText(R.string.viewing_todos);
                entries_list_title.setVisibility(View.VISIBLE);
                noitemtext = getString(R.string.no_unmet_entries);
            } else if (viewing == Viewing.All) {
                Pair<List<Entry>, Integer> listdata = db.getAllEntriesCollapsed(startindex, lengthindex);
                listentry = listdata.first;
                moreentries = listdata.second;
                entries_list_title.setText(R.string.list_all_entries);
                entries_list_title.setVisibility(View.VISIBLE);
                noitemtext = getString(R.string.no_entries);
            }
        } else {
            Pair<List<Entry>, Integer> listdata =  db.getAllEntries(g, startindex, lengthindex);
            listentry = listdata.first;
            moreentries = listdata.second;
            entries_list_title.setText(getString(R.string.list_entries_goal) + g.getName());
            entries_list_title.setVisibility(View.VISIBLE);
            noitemtext = getString(R.string.no_entries);
        }

        if (listentry.size()==0) {
            Toast.makeText(this, getString(R.string.list_no_entries), Toast.LENGTH_SHORT).show();
            entries_list_title.setVisibility(View.GONE);
            Entry noitem = new Entry();
            noitem.setNav(1);
            noitem.setComment(noitemtext);
            listentry.add(noitem);

        }


        if (viewing == Viewing.Unmet) {
            Entry navEntry = new Entry();
            navEntry.setNav(1);
            navEntry.setComment(getString(R.string.previous_entries));
            listentry.add(navEntry);
        } else if (viewing == Viewing.All) {
            if (moreentries>0) {
                Entry navEntry = new Entry();
                navEntry.setNav(1);
                navEntry.setComment(getString(R.string.older_entries));
                listentry.add(navEntry);

            }
            if (startindex>0) {
                Entry navEntry = new Entry();
                navEntry.setNav(-1);
                navEntry.setComment(getString(R.string.newer_entries));
                listentry.add(0, navEntry);

            }
        }

        ListView mainList = (ListView) findViewById(R.id.mainList);

        listitemadapter = new EntryItemArrayAdapter(this, listentry);
        listitemadapter.setEntryItemClickListener(this);

        listitemadapter.setGoallist(g!=null);
        mainList.setAdapter(listitemadapter);



    }

    @Override
    public void viewAllGoalEntriesClick(int goalid) {
        populateList(goalid);
    }
    @Override
    public void addEntryClick(int goalid) {
        showEntryEditorForGoal(goalid);
    }
    @Override
    public void editEntryClick(int entryid) {
        showEntryEditor(entryid);
    }
    @Override
    public void navEntryClick(int navValue) {
        navClicked(navValue);
    }

    @Override
    public void onBackPressed() {
        if (currentGoal != null) {
            populateList();
        } else {

            if (startindex>0) {
                startindex -= lengthindex;
                populateList();
            } else if (viewing == Viewing.All) {
                startindex = 0;
                viewing = Viewing.Unmet;
                populateList();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void navClicked(int navValue) {
        if (navValue==0) return;
        if (viewing == Viewing.Unmet) {
            viewing = Viewing.All;
            startindex = 0;
            populateList();
        } else if (viewing == Viewing.All) {
            //viewing = Viewing.All;
            startindex += lengthindex*navValue;
            populateList();

        }
    }

    private void handleEntryClick(long id, int pos) {

        Entry entry = null;
        if (!listitemadapter.isEmpty() && pos >= 0) entry = listitemadapter.getItem(pos);

        if (entry != null && entry.isNav()) {

            navClicked(entry.getNav());
        } else {
            showEntryEditor(id, pos);
        }
    }


    private int entry_edit_code = 1;

    private void showEntryEditor(long id, int pos) {

        Entry entry = null;
        if (!listitemadapter.isEmpty() && pos >= 0) entry = listitemadapter.getItem(pos);

        if (entry != null && entry.isCollapsed()) {
            populateList(entry.getGoal());
        } else {
            showEntryEditor((int) id);

        }
    }

    private void showEntryEditor(int entryid) {

        Intent entry_edit = new Intent(this, EditEntryActivity.class);

        entry_edit.putExtra(EditEntryActivity.PASSINGENTRYID, entryid);

        this.startActivityForResult(entry_edit, entry_edit_code);
    }

    private void showEntryEditorForGoal(int goalid) {


        Intent entry_edit = new Intent(this, EditEntryActivity.class);

        entry_edit.putExtra(EditEntryActivity.PASSINGGOALID, goalid);

        this.startActivityForResult(entry_edit, entry_edit_code);

    }


    private final int goal_edit_code = 2;

    private void showGoalEditor(long id) {
        Intent goal_edit = new Intent(this, EditGoalActivity.class);

        goal_edit.putExtra(EditGoalActivity.PASSINGGOALID, (int)id);

        this.startActivityForResult(goal_edit, goal_edit_code);
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
        } else if (requestCode == settings_code) {
            setalarm();
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
        } else if (id == R.id.menu_report) {
            showReport();
        } else if (id == R.id.menu_settings) {

            showSettings();

        } else {
            showEntryEditorForGoal(id - 1001);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showReport() {
        Intent report_intent = new Intent(this, ReportActivity.class);
        this.startActivity(report_intent);
    }

    private int settings_code = 23;
    private void showSettings() {
        Intent settings_intent = new Intent(this, SettingsActivity.class);
        this.startActivityForResult(settings_intent, settings_code);

    }

    private void test() {


    }

}
