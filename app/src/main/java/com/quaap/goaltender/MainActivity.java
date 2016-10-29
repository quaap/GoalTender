package com.quaap.goaltender;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static GoalDB db;
    private EntryItemArrayAdapter listitemadapter;

    private Goal currentGoal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // openOptionsMenu();
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.showOverflowMenu();

            }
        });

        db = new GoalDB(this);
        if (db.isFirstRun()) {
            makeDefault();
        }

        populateList();

        ListView mainList = (ListView) findViewById(R.id.mainList);
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEntryEditor(id, position);
            }
        });

    }

    private void populateList() {
        populateList(null);
    }

    private void populateList(Goal g) {
        currentGoal = g;
        List<String> listitems = new ArrayList<>();
        //List<Entry> listentry = db.getAllEntries();
        List<Entry> listentry;
        if (g == null) {
            listentry = db.getUnmetEntries();
            listentry.addAll(db.getAllEntriesCollapsed());
        } else {
            listentry = db.getAllEntries(g);
        }
        for (Entry entry : listentry) {
            listitems.add(entry.getGoal().getName() + " " + entry.getDate().toString());
        }
        ListView mainList = (ListView) findViewById(R.id.mainList);

        listitemadapter = new EntryItemArrayAdapter(this, listitems.toArray(new String[0]), listentry);
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

    @Override
    protected void onStop() {
        db.close();
        super.onStop();
    }

    public static GoalDB getDatabase() {
        if (db == null) {
            throw new NullPointerException("Database not initialized yet!");
        }
        return db;
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


    private int goal_edit_code = 2;

    private void showGoalEditor(long id) {
        Intent goal_edit = new Intent(this, EditGoalActivity.class);

        //goal_edit.putExtra("goal_id", (int)id);

        this.startActivityForResult(goal_edit, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == entry_edit_code && resultCode == RESULT_OK) {
            Toast.makeText(this, "Entry Saved", Toast.LENGTH_SHORT).show();
        } else if (requestCode == goal_edit_code && resultCode == RESULT_OK) {
            Toast.makeText(this, "Goal Saved", Toast.LENGTH_SHORT).show();
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
        GoalDB db = getDatabase();

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
        } else {
            showEntryEditor(id - 1001);
        }

        return super.onOptionsItemSelected(item);
    }

    private static void makeDefault() {
        GoalDB db = getDatabase();

        String gname = "Weight";
        Goal g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Weekly);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(180);
            g.setUnits("lbs");
            g.setMinmax(Goal.MinMax.Maximum);
            db.addGoal(g);
        }

        gname = "Clean kitchen";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.DailyCheckoff);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(1);
            db.addGoal(g);
        }

        gname = "Walking";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.DailyTotal);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(30);
            g.setUnits("mins");
            g.setMinmax(Goal.MinMax.Minimum);
            db.addGoal(g);
        }
        //        Entry e = new Entry();
        //        e.setGoal(g);
        //        e.setDate(new Date());
        //        e.setValue(225);
        //        e.setComment("OMG");
        //        db.addEntry(e);
    }
}
