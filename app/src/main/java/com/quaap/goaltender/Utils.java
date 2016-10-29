package com.quaap.goaltender;

import android.text.format.DateUtils;

import com.quaap.goaltender.storage.Goal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tom on 10/28/16.
 */

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
        switch (type.getPeriod()) {
            case NamedDays:
            case Daily: format="yyyy-MM-dd"; break;
            case Weekly: format="yyyy 'Week' w"; break;
            case Monthly: format="yyyy-MM"; break;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        String fdate = dateFormat.format(date);
        String currentperiod = dateFormat.format(new Date());
        if (fdate.equals(currentperiod)) {
            switch (type.getPeriod()) {
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
}
