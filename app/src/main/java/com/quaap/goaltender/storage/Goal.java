package com.quaap.goaltender.storage;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tom on 10/24/16.
 */

public class Goal {

    private int id = -1;
    private String goalname;
    private Type type;

    private float goalnum;

    private String units;

    private MinMax minmax;

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
        Single(0),
        DailyTotal(1),
        WeeklyTotal(2),
        MonthlyTotal(3),
        //DailyTask(4),
        //WeeklyTask(5),
        //MonthlyTask(6)
        ;


        private int id;

        Type(int id) {
            this.id=id;
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
    };

}
