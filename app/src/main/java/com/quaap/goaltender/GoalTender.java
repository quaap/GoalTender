package com.quaap.goaltender;

import android.app.Application;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


    private final static boolean test = false;

    private static GoalDB db;

    @Override
    public void onCreate() {
        super.onCreate();

        if (test) {
            this.deleteDatabase(GoalDB.DATABASE_NAME + "_test");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        db = new GoalDB(this, test);
        if (db.isFirstRun()) {
            makeDefault();
        }


       // startService(new Intent(this, GoalReminderService.class));

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
        List<Goal> goals = new ArrayList<>();

        boolean isUS = Locale.getDefault() == Locale.US;


        gname = "Clean kitchen";
        Goal g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Checkbox);
            g.setPeriod(Goal.Period.Daily);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(1);
            db.addGoal(g);
        }
        goals.add(g);

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
        goals.add(g);

        gname = "Calories";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Cumulative);
            g.setPeriod(Goal.Period.Daily);
            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(1800);
            g.setUnits("kcal");
            g.setMinmax(Goal.MinMax.Minimum);
            db.addGoal(g);
        }
        goals.add(g);

        gname = "Weight";
        g = db.getGoal(gname);
        if (g == null) {
            g = new Goal();
            g.setType(Goal.Type.Value);
            g.setPeriod(Goal.Period.Weekly);

            g.setStartDate(new Date());
            g.setName(gname);
            g.setGoalnum(isUS?180:80);
            g.setUnits(isUS?"lbs":"kg");
            g.setMinmax(Goal.MinMax.Maximum);
            db.addGoal(g);
        }
        goals.add(g);


        if (test) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);

            Calendar now = Calendar.getInstance();
            now.setTime(new Date());

            for (; cal.before(now); cal.add(Calendar.DAY_OF_YEAR, 1)) {
                Entry e = new Entry();
                e.setDate(cal.getTime());
                Collections.shuffle(goals);
                Goal ge = goals.get(0);

                e.setGoal(ge);
                float gnum = ge.getGoalnum();
                e.setValue((float)(gnum + (Math.random() - .5)*gnum ) );

                db.addEntry(e);

                if (Math.random()>.9) cal.add(Calendar.DAY_OF_YEAR, (int)(Math.random()*9));
            }

        }

    }
}
