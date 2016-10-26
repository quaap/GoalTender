package com.quaap.goaltender.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by tom on 10/24/16.
 */

public class GoalDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Goals";
    private static final int DATABASE_VERSION = 1;


    private static final String GOAL_TABLE = "goals";
    private static final String[] goalcolumns = {"id", "name", "type", "goalnum", "units", "start", "archived"};
    private static final String[] goalcolumntypes = {"INTEGER PRIMARY KEY AUTOINCREMENT", "TEXT", "INTEGER", "FLOAT", "TEXT", "DATETIME", "DATETIME"};
    private static final String GOAL_TABLE_CREATE = buildCreateTableStmt(GOAL_TABLE, goalcolumns, goalcolumntypes);


    private static final String ENTRY_TABLE = "entries";
    private static final String[] entrycolumns = {"id", "goalid", "value", "entrydate", "comment"};
    private static final String[] entrycolumntypes = {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "float", "DATETIME", "TEXT"};
    private static final String ENTRY_TABLE_CREATE = buildCreateTableStmt(ENTRY_TABLE, entrycolumns, entrycolumntypes);

    private static final String[] BASIC_UNITS = {"",
                            "lbs", "pounds", "kg", "kilograms", "oz", "ounces", "nt",
                            "calories", "cal", "kcal", "kilocalories", "kj", "bpm",
                            "feet", "ft", "inches", "in", "cm", "meters", "m", "miles", "mi", "km", "kilometers", "yd",
                            "reps", "sets",
                            "seconds", "minutes", "hours", "days",
                            "s",  "h", "rpm"};

    private Map<Integer,Goal> goals = new HashMap<>();

    public GoalDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GOAL_TABLE_CREATE);
        db.execSQL(ENTRY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public static String formatDateTime(Date date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    public static String formatDateTime(Long date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        return dateFormat.format(new Date(date));
    }

    public static Date getDateTime(String date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static Long dateToLong(Date date) {
        if (date==null || date.equals(new Date(0))) return null;
        return date.getTime();
    }

    public static Date longToDate(Long date) {
        if (date==null || date==0) return null;
        return new Date(date);
    }

    public boolean addGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put("name", goal.getName());
        values.put("type", goal.getType().getID());
        values.put("goalnum", goal.getGoalnum());
        values.put("units", goal.getUnits());
        values.put("start", dateToLong(goal.getStartDate()));
        values.put("archived", dateToLong(goal.getArchiveDate()));

        if (goal.getId()!=-1) {
            db.update(GOAL_TABLE, values, "id=?", new String[]{goal.getId()+""});
        } else {
            goal.setId((int) db.insert(GOAL_TABLE, null, values));
        }

        System.out.println(goal.getId() + " - " + goal.getName());

        goals.put(goal.getId(), goal);

        db.close();

        return true;
    }


    private Goal getGoalFromCursor(Cursor cursor) {
        int id = cursor.getInt(0);
        Goal goal = null;//goals.get(id);
        if (goal!=null) return goal;
        goal = new Goal();
        //{"id", "name", "type", "goalnum", "units", "start", "archived"}
        goal.setId(id);
        goal.setName(cursor.getString(1));
        goal.setType(cursor.getInt(2));
        goal.setGoalnum(cursor.getFloat(3));
        goal.setUnits(cursor.getString(4));
        goal.setStartDate(longToDate(cursor.getLong(5)));
        goal.setArchiveDate(longToDate(cursor.getLong(6)));

        return goal;
    }

    public Goal getGoal(String name) {
        Goal goal = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(GOAL_TABLE, goalcolumns, "name=?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()) {
            goal = getGoalFromCursor(cursor);
        }
        return goal;
    }

    public Goal getGoal(int id) {
        Goal goal = goals.get(id);
        if (goal!=null) return goal;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(GOAL_TABLE, goalcolumns, "id=?", new String[]{id+""}, null, null, null);
        if (cursor.moveToFirst()) {
            goal = getGoalFromCursor(cursor);
        }
        return goal;
    }

    public List<Goal> getAllGoals(boolean archived) {
        List<Goal> goals = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String getarchive = null;
        if (!archived) {
            getarchive = "archived is not null and archived>0";
        }
        Cursor cursor = db.query(GOAL_TABLE, goalcolumns, getarchive, null, null, null,"name, start desc");

        Goal goal;
        if (cursor.moveToFirst()) {
            do {
                goals.add(getGoalFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return goals;
    }

    public List<String> getAllUnits() {
        List<String> units = new ArrayList<>();
        units.addAll(Arrays.asList(BASIC_UNITS));

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(true, GOAL_TABLE, new String[]{"units"}, null , null, null, null, "units", null);
        Goal goal;
        if (cursor.moveToFirst()) {
            do {
                units.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        Collections.sort(units);
        return units;
    }

    public void deleteEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ENTRY_TABLE,"id=?", new String[]{id+""});

    }

    public boolean addEntry(Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put("goalid", entry.getGoal().getId());
        values.put("value", entry.getValue());
        values.put("entrydate", dateToLong(entry.getDate()));
        values.put("comment", entry.getComment());

        if (entry.getId()!=-1) {
            //values.put("id", );
            db.update(ENTRY_TABLE, values, "id=?", new String[] {entry.getId()+""});
        } else {
            entry.setId((int) db.insert(ENTRY_TABLE, null, values));
        }
        return true;
    }

    private Entry getEntryFromCursor(Cursor cursor) {
        Entry entry = new Entry();
        //{"id", "goalid", "value", "entrydate", "comment"};
        entry.setId(cursor.getInt(0));
        entry.setGoal(getGoal(cursor.getInt(1)));
        entry.setValue(cursor.getFloat(2));
        entry.setDate(longToDate(cursor.getLong(3)));
        entry.setComment(cursor.getString(4));
        return entry;
    }

    public Entry getEntry(Goal goal, Date date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                ENTRY_TABLE,
                entrycolumns,
                "goalid=? and date=?",
                new String[]{goal.getId()+"", dateToLong(date).toString()},
                null,
                null,
                null);


        if (cursor.moveToFirst()) {
            return getEntryFromCursor(cursor);
        }
        return null;
    }

    public Entry getEntry(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                ENTRY_TABLE,
                entrycolumns,
                "id=?",
                new String[]{id+""},
                null,
                null,
                null);


        if (cursor.moveToFirst()) {
            return getEntryFromCursor(cursor);
        }
        return null;
    }

    public List<Entry> getAllEntries(Goal goal) {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String getarchive = null;

        Cursor cursor = db.query(ENTRY_TABLE, entrycolumns, "goalid=?", new String[]{goal.getId()+""}, null, null,"entrydate desc");


        if (cursor.moveToFirst()) {
            do {
                entries.add(getEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return entries;
    }

    public List<Entry> getAllEntries() {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String getarchive = null;

        Cursor cursor = db.query(ENTRY_TABLE, entrycolumns, null, null, null, null,"entrydate desc");


        if (cursor.moveToFirst()) {
            do {
                entries.add(getEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return entries;
    }


    public static String buildCreateTableStmt(String tablename, String[] cols, String[] coltypes) {

        String create =  "CREATE TABLE " + tablename + " (";
        for (int i=0; i<cols.length; i++) {
            create += cols[i] + " " + coltypes[i];
            if (i<cols.length-1) create += ", ";
        }
        create += ");";
        return create;

    }


}
