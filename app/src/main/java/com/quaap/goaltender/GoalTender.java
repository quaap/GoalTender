package com.quaap.goaltender;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.quaap.goaltender.service.GoalReminderService;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Date;

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

public class GoalTender  extends Application {


    private static GoalDB db;

    @Override
    public void onCreate() {
        super.onCreate();

        db = new GoalDB(this);
        if (db.isFirstRun()) {
            makeDefault();
        }

        startService(new Intent(this, GoalReminderService.class));

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
