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

    private Date startDate = null;
    private Date archiveDate = null;

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

    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
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


    public static enum Type {
        Single(0),
        Repeating(1);


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
