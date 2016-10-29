package com.quaap.goaltender.storage;

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
    private Type type = Type.Single;

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


    public static enum MinMax {
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

    };

    public static enum Type {
        Single(0, Period.None, false),
        DailyTotal(10, Period.Daily, true),
        NamedDaysTotal(15, Period.NamedDays, true),
        WeeklyTotal(20, Period.Weekly, true),
        MonthlyTotal(30, Period.Monthly, true),
        DailyCheckoff(40, Period.Daily, false, true),
        NamedDaysCheckoff(50, Period.NamedDays, false, true),
        WeeklyCheckoff(60, Period.Weekly, false, true),
        MonthlyCheckoff(70, Period.Monthly, false, true),
        Daily(80, Period.Daily, false),
        NamedDays(90, Period.NamedDays, false),
        Weekly(100, Period.Weekly, false),
        Monthly(110, Period.Monthly, false),
        ;


        final private int id;
        final private Period period;
        final private boolean cumulative;
        final private boolean bool;


        Type(int id, Period period, boolean cumulative) {
            this(id, period, cumulative, false);
        }

        Type(int id, Period period, boolean cumulative, boolean bool) {
            this.id=id;
            this.period=period;
            this.cumulative=cumulative;
            this.bool=bool;
        }


        public static Type get(int id) {
            return lookup.get(id);
        }

        public int getID() {
            return id;
        }

        private static final Map<Integer,Type> lookup = new HashMap<>();
        // Populate the lookup table on loading time
        static {
            for (Type s : EnumSet.allOf(Type.class))
                lookup.put(s.getID(), s);
        }

        public Period getPeriod() {
            return period;
        }

        public boolean isCumulative() {
            return cumulative;
        }

        public boolean isBool() {
            return bool;
        }
    }

    public static enum Period {
        None(0), Hourly(10), Daily(20), NamedDays(30), Weekly(40), Biweekly(50), Monthly(60), Anually(100);
        private final int id;
        private Period(int id) { this.id = id; }
        public int getId() { return id; }
    }

    public static enum Days {
        Sunday(1), Monday(2), Tuesday(4), Wednesday(8), Thursday(16), Friday(32), Saturday(64);
        private final int id;
        private Days(int id) { this.id = id; }
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
