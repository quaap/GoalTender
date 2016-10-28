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
    private static final String[] goalcolumns = {"id", "name", "type", "goalnum", "units", "minmax", "start", "archived"};
    private static final String[] goalcolumntypes = {"INTEGER PRIMARY KEY AUTOINCREMENT", "TEXT", "INTEGER", "FLOAT", "TEXT", "SHORT", "DATETIME", "DATETIME"};
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
                            "seconds", "sec", "minutes", "mins", "hours", "hrs", "days",
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(dbdateformat, Locale.ENGLISH);
        return dateFormat.format(date);
    }

    private static Date dbParseDateTime(String date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(dbdateformat, Locale.ENGLISH);
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    private static final String datepassingformat = "yyyy-MM-dd HH:mm:ss";

    public static String dateToString(Date date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(datepassingformat, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date stringToDate(String date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(datepassingformat, Locale.getDefault());
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

//    public static String dateToString(Long date) {
//        if (date==null) return null;
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        return dateFormat.format(new Date(date));
//    }


//    public static Long dateToLong(Date date) {
//        if (date==null || date.equals(new Date(0))) return null;
//        return date.getTime();
//    }
//
//    public static Date longToDate(Long date) {
//        if (date==null || date==0) return null;
//        return new Date(date);
//    }

    public static String formatDateForDisplay(Date date, Goal.Type type) {
        if (date==null) return null;

        String format="yyyy-MM-dd HH:mm";
        switch (type) {
            case DailyTotal: format="yyyy-MM-dd"; break;
            case WeeklyTotal: format="yyyy-MM 'W'W"; break;
            case MonthlyTotal: format="yyyy-MM"; break;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(date);
    }
    public boolean addGoal(Goal goal) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put("name", goal.getName());
        values.put("type", goal.getType().getID());
        values.put("goalnum", goal.getGoalnum());
        values.put("units", goal.getUnits());
        values.put("minmax", goal.getMinmax().getID());
        values.put("start", dbFormatDateTime(goal.getStartDate()));
        values.put("archived", dbFormatDateTime(goal.getArchiveDate()));

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
        //{"id", "name", "type", "goalnum", "units", "minmax", "start", "archived"};
        goal.setId(id);
        goal.setName(cursor.getString(1));
        goal.setType(cursor.getInt(2));
        goal.setGoalnum(cursor.getFloat(3));
        goal.setUnits(cursor.getString(4));
        goal.setMinmax(cursor.getInt(5));
        goal.setStartDate(dbParseDateTime(cursor.getString(6)));
        goal.setArchiveDate(dbParseDateTime(cursor.getString(7)));

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
        String getarchive = null;
        if (onlyopen) {
            getarchive = "archived IS NULL";
        }
        Cursor cursor = db.query(GOAL_TABLE, goalcolumns, getarchive, null, null, null,"name, start desc");

        Goal goal;
        if (cursor.moveToFirst()) {
            do {
                goals.add(getGoalFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return goals;
    }

    public List<Entry> getUnmetEntries() {
        List<Entry> entries = new ArrayList<>();
        for (Goal g: getUnmetGoals()) {
            Entry entry = new Entry();
            entry.setGoal(g);
            entry.setValue(0);
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
        Goal goal;
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
        goals.addAll(getGoalsFromSQL("select g.id " +
                "from goals g " +
                "where g.type=" + Goal.Type.DailyTotal.getID() + " and g.id not in " +
                "(select distinct goalid id " +
                " from entries e " +
                " where strftime('%Y-%m-%d', e.entrydate) = strftime('%Y-%m-%d', date('now'))" +
                ")",null));

        // get Weekly Goals
        goals.addAll(getGoalsFromSQL("select g.id " +
                "from goals g " +
                "where g.type=" + Goal.Type.WeeklyTotal.getID() + " and g.id not in " +
                "(select distinct goalid id " +
                " from entries e " +
                " where strftime('%Y-%W', e.entrydate) = strftime('%Y-%W', date('now'))" +
                ")",null));

        // get Weekly Goals
        goals.addAll(getGoalsFromSQL("select g.id " +
                "from goals g " +
                "where g.type=" + Goal.Type.MonthlyTotal.getID() + " and g.id not in " +
                "(select distinct goalid id " +
                " from entries e " +
                " where strftime('%Y-%m', e.entrydate) = strftime('%Y-%m', date('now'))" +
                ")",null));


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



    public static String getRoundedDate(Date date, Goal.Type gtype) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());


        if(gtype!=Goal.Type.Single) {
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            if(gtype==Goal.Type.MonthlyTotal) { //month
                cal.set(Calendar.DAY_OF_MONTH, 1);
            } else if(gtype==Goal.Type.WeeklyTotal) { //week
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            }
            //day
            date = new Date(cal.getTimeInMillis());
        }
        return dateToString(date);

    }

//    public static void  main(String [] args) {
//        Date date = new Date();
//        System.out.println(getRoundedDate(date, Goal.Type.Single));
//        System.out.println(getRoundedDate(date, Goal.Type.DailyTotal));
//        System.out.println(getRoundedDate(date, Goal.Type.WeeklyTotal));
//        System.out.println(getRoundedDate(date, Goal.Type.MonthlyTotal));
//    }

    public List<Entry> getAllEntriesCollapsed() {

        Map<String, Entry> collapsedmap = new TreeMap<>();

        for (Entry entry: getAllEntries()) {
            Goal goal = entry.getGoal();
           // if (goal.getArchiveDate()!=null) continue;

            String key = getRoundedDate(entry.getDate(), goal.getType()) + goal.getName() + goal.getType().name();

            if(goal.getType()==Goal.Type.Single) {
                key += entry.getId() + "";
            }
            System.out.println(key + " " +entry.getDate() );
            Entry e = collapsedmap.get(key);
            if (e!=null) {
                e.setValue(e.getValue() + entry.getValue());
                e.incrementCollapsednum();
            } else {
                if(goal.getType()==Goal.Type.Single) {
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
        return collapsed;
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
        cursor.close();
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


    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }
}
