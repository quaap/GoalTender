package com.quaap.goaltender.storage;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quaap.goaltender.GoalTender;
import com.quaap.goaltender.MainActivity;
import com.quaap.goaltender.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tom on 10/24/16.
 */

public class Goal {

    private int id = -1;
    private String goalname;
    private Type type = Type.Value;
    private Period period = Period.None;

    private float goalnum = 0;

    private String units = "";

    private MinMax minmax = MinMax.Minimum;

    private List<Days> days = new ArrayList<>();

    private Date startDate = null;


    private boolean active = true;

    public String getName() {
        return goalname;
    }

    public void setName(String goalname) {
        this.goalname = goalname;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setType(int id) {
        this.type = Type.get(id);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public float getGoalnum() {
        return goalnum;
    }

    public void setGoalnum(float goalnum) {
        this.goalnum = goalnum;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public MinMax getMinmax() {
        return minmax;
    }

    public void setMinmax(MinMax minmax) {
        this.minmax = minmax;
    }
    public void setMinmax(int id) {
        this.minmax = MinMax.get(id);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Days> getDays() {
        return days;
    }

    public void setDays(List<Days> days) {
        this.days = days;
    }

    public void setDays(int dayflags) {
        this.days = Days.split(dayflags);
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = Period.get(period);
    }
    public void setPeriod(Period period) {
        this.period = period;
    }


    public enum MinMax {
        Minimum(0), Maximum(1);
        private int id;
        MinMax(int id) {
            this.id = id;
        }

        public static MinMax get(int id) {
            return lookup.get(id);
        }

        public int getID() {
            return id;
        }
        private static final Map<Integer,MinMax> lookup = new HashMap<>();
        // Populate the lookup table on loading time
        static {
            for (MinMax s : EnumSet.allOf(MinMax.class))
                lookup.put(s.getID(), s);
        }

    }

//    public enum Type {
//        Single(0, Period.None, false),
//        DailyTotal(10, Period.Daily, true),
//        NamedDaysTotal(15, Period.NamedDays, true),
//        WeeklyTotal(20, Period.Weekly, true),
//        MonthlyTotal(30, Period.Monthly, true),
//        DailyCheckoff(40, Period.Daily, false, true),
//        NamedDaysCheckoff(50, Period.NamedDays, false, true),
//        WeeklyCheckoff(60, Period.Weekly, false, true),
//        MonthlyCheckoff(70, Period.Monthly, false, true),
//        Daily(80, Period.Daily, false),
//        NamedDays(90, Period.NamedDays, false),
//        Weekly(100, Period.Weekly, false),
//        Monthly(110, Period.Monthly, false),
//        ;
//
//
//        final private int id;
//        final private Period period;
//        final private boolean cumulative;
//        final private boolean bool;
//
//
//        Type(int id, Period period, boolean cumulative) {
//            this(id, period, cumulative, false);
//        }
//
//        Type(int id, Period period, boolean cumulative, boolean bool) {
//            this.id=id;
//            this.period=period;
//            this.cumulative=cumulative;
//            this.bool=bool;
//        }
//
//
//        public static Type get(int id) {
//            return lookup.get(id);
//        }
//
//        public int getID() {
//            return id;
//        }
//
//        private static final Map<Integer,Type> lookup = new HashMap<>();
//        // Populate the lookup table on loading time
//        static {
//            for (Type s : EnumSet.allOf(Type.class))
//                lookup.put(s.getID(), s);
//        }
//
//        public Period getPeriod() {
//            return period;
//        }
//
//        public boolean isCumulative() {
//            return cumulative;
//        }
//
//        public boolean isBool() {
//            return bool;
//        }
//    }

    public enum Type {
        Value(10, R.string.type_value),
        Cumulative(20,R.string.type_cumulative),
        Checkbox(30,R.string.type_checkbox);

        private final int id;
        private final int displayid;

        Type(int id, int displayid) {
            this.id = id;
            this.displayid = displayid;
        }
        public int getId() { return id; }

        public static Type get(int id) {
           return lookup.get(id);
        }

//        @Override
//        public String toString() {
//            return GoalTender.getContext().getString(displayid);
//
//        }

        private static final Map<Integer,Type> lookup = new HashMap<>();
        static {
            for (Type s : EnumSet.allOf(Type.class))
                lookup.put(s.getId(), s);
        }

        public static ArrayAdapter<Type> getArrayAdapter(final Context context, int resource) {
            ArrayAdapter<Type> aa = new ArrayAdapter<Type>(context, resource, Type.values()) {
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView tv= new TextView(this.getContext());
                    tv.setText(this.getContext().getText(this.getItem(position).displayid));
                    return tv;
                    //return super.getView(position, convertView, parent);
                }
            };
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return aa;
        }
    }

    public enum Period {
        None(0, R.string.period_none),
        Daily(20, R.string.period_daily),
        NamedDays(30, R.string.period_namedDays),
        Weekly(40, R.string.period_weekly),
        Monthly(60, R.string.period_monthly);

        private final int id;
        private final int displayid;
        Period(int id, int displayid) {
            this.id = id;
            this.displayid = displayid;
        }
        public int getId() { return id; }


        public static Period get(int id) {
            return lookup.get(id);
        }
        private static final Map<Integer,Period> lookup = new HashMap<>();
        static {
            for (Period s : EnumSet.allOf(Period.class))
                lookup.put(s.getId(), s);
        }

        public static ArrayAdapter<Period> getArrayAdapter(final Context context, int resource) {
            ArrayAdapter<Period> aa = new ArrayAdapter<Period>(context, resource, Period.values()) {
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    TextView tv= new TextView(this.getContext());
                    tv.setText(this.getContext().getText(this.getItem(position).displayid));
                    return tv;
                    //return super.getView(position, convertView, parent);
                }
            };
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return aa;
        }
    }

    public enum Days {
        Sunday(1, R.string.days_sunday),
        Monday(2, R.string.days_monday),
        Tuesday(4, R.string.days_tuesday),
        Wednesday(8, R.string.days_wednesday),
        Thursday(16, R.string.days_thursday),
        Friday(32, R.string.days_friday),
        Saturday(64, R.string.days_saturday);

        private final int id;
        private final int displayid;
        Days(int id, int displayid) {
            this.id = id;
            this.displayid = displayid;
        }
        public int getId() { return id; }

        public static int combine(EnumSet<Days> days) {
            return combine(days.toArray(new Days[days.size()]));
        }

        public static int combine(List<Days> days) {
            return combine(days.toArray(new Days[days.size()]));
        }

        public static int combine(Days ... days) {
            int flags = 0;
            for (Days day: days) {
                flags |= day.getId();
            }
            return flags;
        }

        public static int add(int flags, Days day) {
            return flags | day.getId();
        }

        public static boolean contains(int flags, Days day) {
            return (flags & day.getId())==day.getId();
        }

        public static List<Days> split(Integer flags) {
            List<Days> days = new ArrayList<>();

            if (flags==null) {
                return days;
            }

            for (Days day: EnumSet.allOf(Days.class)) {
                if (contains(flags, day)) {
                    days.add(day);
                }
            }

            return days;
        }

    }

}
