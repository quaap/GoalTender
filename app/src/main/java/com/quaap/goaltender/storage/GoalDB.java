package com.quaap.goaltender.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.quaap.goaltender.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by tom on 10/24/16.
 */

public class GoalDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Goals";
    private static final int DATABASE_VERSION = 1;


    private static final String GOAL_TABLE = "goals";
    private static final String[] goalcolumns = {"id", "name", "type", "period", "days", "goalnum", "units", "minmax", "start",  "active"};
    private static final String[] goalcolumntypes = {"INTEGER PRIMARY KEY AUTOINCREMENT", "TEXT", "INTEGER", "INTEGER", "INTEGER", "FLOAT", "TEXT", "SHORT", "DATETIME", "SHORT"};
    private static final String GOAL_TABLE_CREATE = buildCreateTableStmt(GOAL_TABLE, goalcolumns, goalcolumntypes);


    private static final String ENTRY_TABLE = "entries";
    private static final String[] entrycolumns = {"id", "goalid", "value", "entrydate", "comment"};
    private static final String[] entrycolumntypes = {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER", "float", "DATETIME", "TEXT"};
    private static final String ENTRY_TABLE_CREATE = buildCreateTableStmt(ENTRY_TABLE, entrycolumns, entrycolumntypes);

    private static final String[] BASIC_UNITS = {
                            "lbs", "pounds", "kg", "oz", "ounces", "nt",
                            "calories", "cal", "kcal", "kj", "bpm",
                            "feet", "ft", "inches", "in", "cm", "meters", "m", "miles", "mi", "km", "yd",
                            "reps", "sets",
                            "sec","secs", "min", "mins", "hours", "hrs", "days",
                            "s",  "h", "rpm"};

    private Map<Integer,Goal> goals = new HashMap<>();

    private boolean firstRun = false;

    public GoalDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (getAllGoals(true).size()==0) {
            setFirstRun(true);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GOAL_TABLE_CREATE);
        db.execSQL(ENTRY_TABLE_CREATE);
        setFirstRun(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private static final String dbdateformat = "yyyy-MM-dd HH:mm:ss";

    private static String dbFormatDateTime(Date date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dbdateformat, Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    private static Date dbParseDateTime(String date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dbdateformat, Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }


    public boolean addGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put("name", goal.getName());
        values.put("type", goal.getType().getId());
        values.put("period", goal.getPeriod().getId());
        values.put("days", Goal.Days.combine(goal.getDays()));
        values.put("goalnum", goal.getGoalnum());
        values.put("units", goal.getUnits());
        values.put("minmax", goal.getMinmax().getID());
        values.put("start", dbFormatDateTime(goal.getStartDate()));
        values.put("active", goal.isActive());


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

    public void deleteGoal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(ENTRY_TABLE,"goalid=?", new String[]{id+""});
        db.delete(GOAL_TABLE,"id=?", new String[]{id+""});


    }

    private Goal getGoalFromCursor(Cursor cursor) {
        int id = cursor.getInt(0);
        Goal goal = goals.get(id);
        if (goal!=null) return goal;
        goal = new Goal();
        //{"id", "name", "type", "period", "days" "goalnum", "units", "minmax", "start",  "active"}
        goal.setId(id);
        goal.setName(cursor.getString(1));
        goal.setType(cursor.getInt(2));
        goal.setPeriod(cursor.getInt(3));
        goal.setDays(cursor.getInt(4));
        goal.setGoalnum(cursor.getFloat(5));
        goal.setUnits(cursor.getString(6));
        goal.setMinmax(cursor.getInt(7));
        goal.setStartDate(dbParseDateTime(cursor.getString(8)));
        goal.setActive(cursor.getShort(9)==1);


        return goal;
    }

    public Goal getGoal(String name) {
        Goal goal = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(GOAL_TABLE, goalcolumns, "name=?", new String[]{name}, null, null, null);
        if (cursor.moveToFirst()) {
            goal = getGoalFromCursor(cursor);
        }
        cursor.close();
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
        cursor.close();
        return goal;
    }

    public List<Goal> getAllGoals(boolean onlyopen) {
        List<Goal> goals = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        String getactive = null;
        if (onlyopen) {
            getactive = "active = 1";
        }
        Cursor cursor = db.query(GOAL_TABLE, goalcolumns, getactive, null, null, null,"name, start desc");

        if (cursor.moveToFirst()) {
            do {
                goals.add(getGoalFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return goals;
    }


    public void export() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(GOAL_TABLE, null, null, null, null, null, null);
        Utils.CurorToCSV(cursor, "goals.csv");
        cursor = db.query(ENTRY_TABLE, null, null, null, null, null, null);
        Utils.CurorToCSV(cursor, "entries.csv");


//        cursor = db.rawQuery(
//                "select g.id, g.name, g. " +
//                        "from goals g " +
//                        "  join entries e on g.id=e.goalid", null);
        //{"id", "name", "type", "period", "days", "goalnum", "units", "minmax", "start",  "active"};
        //{"id", "goalid", "value", "entrydate", "comment"};

    }




    public List<Entry> getUnmetEntries() {
        List<Entry> entries = new ArrayList<>();
        for (Goal g: getUnmetGoals()) {
            Entry entry = new Entry();
            entry.setGoal(g);
            //entry.setValue(0);
            entry.setDate(new Date());
            entry.setUnmet(true);
            entries.add(entry);

        }

        return entries;
    }

    private List<Goal> getGoalsFromSQL(String sql, String[] selargs) {
        List<Goal> goals = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql,selargs);

        if (cursor.moveToFirst()) {
            do {
                goals.add(getGoal(cursor.getInt(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return goals;
    }

    public List<Goal> getUnmetGoals() {
        List<Goal> goals = new ArrayList<>();
        //get Daily Goals
        goals.addAll(getGoalsFromSQL(
                "select g.id " +
                "from goals g " +
                "where g.period=" + Goal.Period.Daily.getId() + " and g.active=1 and g.id not in " +
                "(select distinct goalid id " +
                " from entries e " +
                " where strftime('%Y-%m-%d', e.entrydate, 'localtime') = strftime('%Y-%m-%d', 'now', 'localtime')" +
                ")",null));

        // get NamedDays Goals
        goals.addAll(getGoalsFromSQL( "select g.id " +
                        "from goals g " +
                        "where g.period=" +  Goal.Period.NamedDays.getId() + " and g.active=1 and " +
                        " (" +
                        "   ((g.days & " + Goal.Days.Sunday.getId() + ") = " + Goal.Days.Sunday.getId() + " and strftime('%w', 'now', 'localtime')='0' " + ") or "+
                        "   ((g.days & " + Goal.Days.Monday.getId() + ") = " + Goal.Days.Monday.getId() + " and strftime('%w', 'now', 'localtime')='1' " + ") or "+
                        "   ((g.days & " + Goal.Days.Tuesday.getId() + ") = " + Goal.Days.Tuesday.getId() + " and strftime('%w', 'now', 'localtime')='2' " + ") or "+
                        "   ((g.days & " + Goal.Days.Wednesday.getId() + ") = " + Goal.Days.Wednesday.getId() + " and strftime('%w', 'now', 'localtime')='3' " + ") or "+
                        "   ((g.days & " + Goal.Days.Thursday.getId() + ") = " + Goal.Days.Thursday.getId() + " and strftime('%w', 'now', 'localtime')='4' " + ") or "+
                        "   ((g.days & " + Goal.Days.Friday.getId() + ") = " + Goal.Days.Friday.getId() + " and strftime('%w', 'now', 'localtime')='5' " + ") or "+
                        "   ((g.days & " + Goal.Days.Saturday.getId() + ") = " + Goal.Days.Saturday.getId() + " and strftime('%w', 'now', 'localtime')='6' " + ")  "+
                        " ) and " +
                        " g.id not in " +
                        "(select distinct goalid id " +
                        " from entries e " +
                        " where strftime('%Y-%m-%d', e.entrydate, 'localtime') = strftime('%Y-%m-%d', 'now', 'localtime')" +
                        ")" ,null));

        // get Weekly Goals
        goals.addAll(getGoalsFromSQL(
                "select g.id " +
                        "from goals g " +
                        "where g.period=" +  Goal.Period.Weekly.getId() + " and g.active=1 and g.id not in " +
                        "(select distinct goalid id " +
                        " from entries e " +
                        " where strftime('%Y-%W', e.entrydate, 'localtime') = strftime('%Y-%W', 'now', 'localtime')" +
                        ")",null));

        // get Monthly Goals
        goals.addAll(getGoalsFromSQL(
                "select g.id " +
                "from goals g " +
                "where g.period=" +  Goal.Period.Monthly.getId() + " and g.active=1 and g.id not in " +
                "(select distinct goalid id " +
                " from entries e " +
                " where strftime('%Y-%m', e.entrydate) = strftime('%Y-%m', 'now', 'localtime')" +
                ")",null));


        return goals;
    }

    public List<String> getAllUnits() {
        List<String> units = new ArrayList<>();
        units.addAll(Arrays.asList(BASIC_UNITS));

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(true, GOAL_TABLE, new String[]{"units"}, null , null, null, null, "units", null);

        if (cursor.moveToFirst()) {
            do {
                String unit = cursor.getString(0);
                if (unit != null && unit.length() > 0) units.add(unit);
            } while (cursor.moveToNext());
        }
        cursor.close();
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
        values.put("entrydate", dbFormatDateTime(entry.getDate()));
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
        entry.setDate(dbParseDateTime(cursor.getString(3)));
        entry.setComment(cursor.getString(4));
        return entry;
    }
//
//    public Entry getEntry(Goal goal, Date date) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.query(
//                ENTRY_TABLE,
//                entrycolumns,
//                "goalid=? and date=?",
//                new String[]{goal.getId()+"", dateToLong(date).toString()},
//                null,
//                null,
//                null);
//
//
//        if (cursor.moveToFirst()) {
//            return getEntryFromCursor(cursor);
//        }
//        return null;
//    }

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



    public static String getRoundedDate(Date date, Goal g) {
        if (date==null) return "isnull";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());


        if(g.getType() == Goal.Type.Cumulative) {
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            if(g.getPeriod()==Goal.Period.Monthly) { //month
                cal.set(Calendar.DAY_OF_MONTH, 1);
            } else if(g.getPeriod()==Goal.Period.Weekly) { //week
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            }
            //day
            date = new Date(cal.getTimeInMillis());
        }
        return Utils.dateToString(date);

    }

//    public static void  main(String [] args) {
//        Date date = new Date();
//        System.out.println(getRoundedDate(date, Goal.Type.Single));
//        System.out.println(getRoundedDate(date, Goal.Type.DailyTotal));
//        System.out.println(getRoundedDate(date, Goal.Type.WeeklyTotal));
//        System.out.println(getRoundedDate(date, Goal.Type.MonthlyTotal));
//    }

    public List<Entry> getAllEntriesCollapsed() {
        return getAllEntriesCollapsed(0,Integer.MAX_VALUE);
    }

    public List<Entry> getAllEntriesCollapsed(int start, int length) {

        Map<String, Entry> collapsedmap = new TreeMap<>();

        for (Entry entry: getAllEntries()) {
            Goal goal = entry.getGoal();
            if (!goal.isActive()) continue;

            String key = getRoundedDate(entry.getDate(), goal) + goal.getName() + goal.getType().name();

            if(goal.getType() != Goal.Type.Cumulative) {
                key += entry.getId() + "";
            }
            System.out.println(key + " " +entry.getDate() );
            Entry e = collapsedmap.get(key);
            if (e!=null) {
                e.setValue(e.getValue() + entry.getValue());
                e.incrementCollapsednum();
            } else {
                if(goal.getType() != Goal.Type.Cumulative) {
                    e = entry;
                } else {
                    e = new Entry(entry);
                    e.setCollapsed(true);
                }
                collapsedmap.put(key,e);
            }

        }
        List<Entry> collapsed = new ArrayList<>(new LinkedHashSet<>(collapsedmap.values()));

        Collections.reverse(collapsed);

        int end = start+length;
        int clen = collapsed.size();
        if (start >= clen) {
            return new ArrayList<>();
        }
        if (end >= clen) {
            end = clen-1;
        }

        return collapsed.subList(start, end);
    }


    public List<Entry> getAllEntries(Goal goal) {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.query(ENTRY_TABLE, entrycolumns, "goalid=?", new String[]{goal.getId()+""}, null, null,"entrydate desc");


        if (cursor.moveToFirst()) {
            do {
                entries.add(getEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return entries;
    }

    public List<Entry> getAllEntries() {
        List<Entry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(ENTRY_TABLE, entrycolumns, null, null, null, null,"entrydate desc");


        if (cursor.moveToFirst()) {
            do {
                entries.add(getEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        return entries;
    }


    private static String buildCreateTableStmt(String tablename, String[] cols, String[] coltypes) {

        String create =  "CREATE TABLE " + tablename + " (";
        for (int i=0; i<cols.length; i++) {
            create += cols[i] + " " + coltypes[i];
            if (i<cols.length-1) create += ", ";
        }
        create += ");";
        return create;

    }


    public boolean isFirstRun() {
        return firstRun;
    }

    private void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }
}
