package com.quaap.goaltender;

import android.app.Application;
import android.content.Context;

import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Date;

/**
 * Created by tom on 11/1/16.
 */

public class GoalTender  extends Application {


    private static GoalDB db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = new GoalDB(this);
        if (db.isFirstRun()) {
            makeDefault();
        }

    }

    @Override
    public void onTerminate() {
        db.close();
        super.onTerminate();
    }


    public static GoalDB getDatabase() {
        if (db == null) {
            throw new NullPointerException("Database not initialized yet!");
        }
        return db;
    }


    private static void makeDefault() {
        GoalDB db = getDatabase();

        String gname;
        Goal g;

        gname = "Clean kitchen";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Checkbox);
            g.setPeriod(Goal.Period.Daily);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(1);
            db.addGoal(g);
        }

        gname = "Walking";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();

            g.setType(Goal.Type.Cumulative);
            g.setPeriod(Goal.Period.Daily);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(30);
            g.setUnits("mins");
            g.setMinmax(Goal.MinMax.Minimum);
            db.addGoal(g);
        }

        gname = "Calories";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Cumulative);
            g.setPeriod(Goal.Period.Daily);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(2400);
            g.setUnits("kcal");
            g.setMinmax(Goal.MinMax.Minimum);
            db.addGoal(g);
        }

        gname = "Weight";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Value);
            g.setPeriod(Goal.Period.Weekly);

            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(180);
            g.setUnits("lbs");
            g.setMinmax(Goal.MinMax.Maximum);
            db.addGoal(g);
        }


        //        Entry e = new Entry();
        //        e.setGoal(g);
        //        e.setDate(new Date());
        //        e.setValue(225);
        //        e.setComment("OMG");
        //        db.addEntry(e);
    }
}
