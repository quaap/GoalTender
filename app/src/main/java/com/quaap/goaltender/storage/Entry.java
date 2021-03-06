package com.quaap.goaltender.storage;

import java.util.Date;

/**
 * Created by tom on 10/24/16.
 */

public class Entry {
    private int id = -1;
    private Goal goal;
    private Date date;
    private float value;
    private String comment;
    private boolean collapsed = false;
    private int collapsednum = 0;

    private boolean unmet=false;

    private int nav = 0;

    public  Entry() {}

    public Entry(Entry other) {
        goal = other.getGoal();
        date = other.getDate();
        value = other.getValue();
        comment = other.getComment();
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Date getDate() {
        if (date==null) return new Date(0);
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        incrementCollapsednum();
    }

    public int getCollapsednum() {
        return collapsednum;
    }

    public void incrementCollapsednum() {
        this.collapsednum++;
    }

    public boolean isUnmet() {
        return unmet;
    }

    public void setUnmet(boolean unmet) {
        this.unmet = unmet;
    }

    public boolean isNav() {
        return nav!=0;
    }

    public int getNav() {
        return nav;
    }

    public void setNav(int nav) {
        this.nav = nav;
    }
}
