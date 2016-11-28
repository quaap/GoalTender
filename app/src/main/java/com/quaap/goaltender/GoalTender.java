package com.quaap.goaltender;


import android.app.Application;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.quaap.goaltender.notify.NotifyService;
import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    private static boolean running;

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        GoalTender.running = running;
    }

    @Override
    public void onCreate() {
        super.onCreate();



        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

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
            String cc = "gg";
            TelephonyManager tm = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
            if (tm!=null) {
                cc = tm.getNetworkCountryIso();
            }
            if (cc==null || cc=="") {
                cc = Locale.getDefault().getCountry();
            }

            makeDefault(cc);
        }

        Intent intent = new Intent(this, NotifyService.class);
        intent.putExtra(NotifyService.CMD, NotifyService.CMD_SETALARM);

        startService(intent);

    }

    @Override
    public void onTerminate() {
        try {
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onTerminate();
    }


    public static GoalDB getDatabase() {
        if (db == null) {
            throw new NullPointerException("Database not initialized yet!");
        }
        return db;
    }




    private static void makeDefault(String cc) {
        GoalDB db = getDatabase();

        String gname;
        List<Goal> goals = new ArrayList<>();

//        boolean isUS = Locale.getDefault() == Locale.US;

        boolean isUS = cc.toLowerCase() == "us";



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

            Map<Goal,Float> lasts = new HashMap<>();
            for (; cal.before(now); cal.add(Calendar.DAY_OF_YEAR, 1)) {

                int num = Utils.getRand(1,3);
                for (int i=0; i<num; i++) {
                    Entry e = new Entry();
                    e.setDate(cal.getTime());
                    Collections.shuffle(goals);
                    Goal ge = goals.get(0);

                    e.setGoal(ge);

                    Float gnum = lasts.get(ge);
                    if (gnum==null) {
                        gnum = ge.getGoalnum();
                        gnum += (float)(Math.random() - .5)/4 * gnum;
                    }
                    float val = (float) (gnum + ((Math.random() - .5)/30) * gnum);

                    if (ge.getType() == Goal.Type.Checkbox) {
                        val = Utils.getRand(0,1);
                    }
                    e.setValue(val);

                    lasts.put(ge,val);
                    db.addEntry(e);
                }

                if (Math.random()>.9) cal.add(Calendar.DAY_OF_YEAR, (int)(Math.random()*4));
            }

        }

    }
}
