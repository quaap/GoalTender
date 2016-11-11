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


import android.database.Cursor;
import android.os.Environment;
import android.text.format.DateUtils;

import com.quaap.goaltender.storage.Goal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utils {

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


    public static String showDate(Date date) {
        if (date==null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMMM d, yyyy", Locale.getDefault());
        return dateFormat.format(date);
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

    public static Date getNextDate(Date date, Goal.Period period) {
        if (date==null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());

        switch (period) {
            case NamedDays:
            case Daily: cal.add(Calendar.DAY_OF_YEAR,1); break;
            case Weekly: cal.add(Calendar.WEEK_OF_YEAR,1); break;
            case Monthly: cal.add(Calendar.MONTH,1); break;
        }
        return cal.getTime();
    }

    public static String formatDateForBucket(Date date, Goal.Period period) {
        if (date==null) return null;

        String format="yyyy-MM-dd HH:mm";
        switch (period) {
            case NamedDays:
            case Daily: format="yyyy-MM-dd"; break;
            case Weekly: format="yyyy 'W'w"; break;
            case Monthly: format="yyyy-MM"; break;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String fdate = dateFormat.format(date);
        return fdate;
    }

    public static String formatDateForShortDisplay(Date date, Goal.Period period) {
        if (date==null) return null;

        String format="MM-dd";
        switch (period) {
            case NamedDays:
            case Daily: format="MMM d"; break;
            case Weekly: format="MMM d";
                Calendar cal=Calendar.getInstance();
                cal.setTime(date);
                cal.add( Calendar.DAY_OF_WEEK, -(cal.get(Calendar.DAY_OF_WEEK)-1));
                date = cal.getTime();
                break;
            case Monthly: format="MMM"; break;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String fdate = dateFormat.format(date);
        return fdate;
    }

    public static String formatDateForDisplay(Date date, Goal.Period period) {
        if (date==null) return null;

        String format="yyyy-MM-dd HH:mm";
        switch (period) {
            case NamedDays:
            case Daily: format="yyyy-MM-dd"; break;
            case Weekly: format="yyyy 'Week' w"; break;
            case Monthly: format="yyyy-MM"; break;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String fdate = dateFormat.format(date);
        String currentperiod = dateFormat.format(new Date());
        if (fdate.equals(currentperiod)) {
            switch (period) {
                case NamedDays:
                case Daily: fdate="Today"; break;
                case Weekly: fdate="This week"; break;
                case Monthly: fdate="This month"; break;
            }
        } else {
            //if  (type.getPeriod() == Goal.Period.Daily) {
                fdate = DateUtils.getRelativeTimeSpanString(date.getTime(), new Date().getTime(), DateUtils.DAY_IN_MILLIS).toString();
            //}
        }
        return fdate;
    }


    public static void CurorToCSV(Cursor cursor, String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
        try (FileWriter goalswriter = new FileWriter(file)) {
            if (cursor.moveToFirst()) {
                int count  = cursor.getColumnCount();
                for (int i=0; i<count; i++) {
                    String text = cursor.getColumnName(i);
                    goalswriter.write(valueToCsvValue(text));
                    if (i<count-1) goalswriter.write(",");
                }
                goalswriter.write("\n");
                do {
                    for (int i=0; i<count; i++) {
                        String text = cursor.getString(i);
                        if (cursor.isNull(i)) {
                            text=null;
                        }
                        goalswriter.write(valueToCsvValue(text));
                        if (i<count-1) goalswriter.write(",");
                    }
                    goalswriter.write("\n");

                } while (cursor.moveToNext());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String valueToCsvValue(String value) {
        if (value==null) {
            return "";
        }
        if (value.length() ==0) {
            return "\"\"";
        }
        if (value.matches("^-?(\\d+(\\.\\d+)?|\\.\\d+)$")) {
            return value;
        }
        return "\"" + value.replaceAll("\"", "\"\"") + "\"";
    }

    public static class MapOfLists<K,V> {

        private Map<K,List<V>> basemap = new HashMap<K,List<V>>();

        public V put(K key, V value) {
            List<V> values = basemap.get(key);
            if (values == null) {
                values = new ArrayList<>();
                basemap.put(key, values);
            }
            values.add(value);

            return value;
        }

        public V get(K key) {
            List<V> values = basemap.get(key);
            if (values==null) {
                return null;
            }
            return values.get(values.size()-1);
        }

        public List<V> getAll(K key) {
            List<V> values = basemap.get(key);
            if (values==null) {
                return null;
            }
            return Collections.unmodifiableList(values);
        }

    }

}
