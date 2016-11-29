package com.quaap.goaltender.notify;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.quaap.goaltender.GoalTender;
import com.quaap.goaltender.MainActivity;
import com.quaap.goaltender.R;
import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

public class NotifyService extends Service {

    public static final String CMD = "cmd";
    public static final int CMD_SETALARM=10;
    public static final int CMD_KILLALARM=20;
    public static final int CMD_NOTIFY=30;
    public static final int CMD_KILLNOTIFY=40;
    public final static int notificationID = 100;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent = null;

    private boolean notify = true;
    private int notifyhours = 12;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        notify = appPreferences.getBoolean("notify", true);

        int hours = Integer.parseInt(appPreferences.getString("notify_hours", "12"));
        if (notifyhours!=hours) {

            notifyhours = hours;
            killAlarm();
            setAlarm();
        }

        int cmd = intent.getIntExtra(CMD, 0);
        Log.d("Ns", "NotifyService.onStartCommand cmd=" + cmd);
//        Log.d("Ns", "notify=" + notify);
//        Log.d("Ns", "notifyhours=" + notifyhours);
        if (cmd==CMD_SETALARM) {
            setAlarm();
        } else if (cmd==CMD_KILLALARM) {
            killAlarm();
        } else if (cmd==CMD_NOTIFY) {
            showNotify();
        } else if (cmd==CMD_KILLNOTIFY) {
            killNotify();
        }

        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    public void  killAlarm() {

        if (alarmIntent!= null ) {
            alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(alarmIntent);
            alarmIntent = null;
        }
    }


    public void setAlarm() {

        killNotify();
        killAlarm();

        if (notify) {
            alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            Intent notifyintent = new Intent(this.getApplicationContext(), AutoStart.class);

            notifyintent.putExtra(NotifyService.CMD, NotifyService.CMD_NOTIFY);

            alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notifyintent, 0);


            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 15);

            alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR*notifyhours, alarmIntent);
            Log.d("Ns", "alarm set");
        }
    }

    public void killNotify() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationID);
    }


    private static Map<String,Long> seen = new HashMap<>();

    public void showNotify() {

        killNotify();
        if (!notify) {
            killAlarm();
            return;
        }

        //Don't show notification if the UI is up.
        if (!GoalTender.isRunning()) return;

        GoalDB db = GoalTender.getDatabase();

        List<Entry> unmets = db.getUnmetEntries();

        int dayofweek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < unmets.size(); i++) {
            String gname = unmets.get(i).getGoal().getName();
            boolean rmday = dayofweek % 2 == 0;
            switch (unmets.get(i).getGoal().getPeriod()) {
                case Monthly:
                    rmday = dayofweek > 1;
                case Weekly:
                    Long sw = seen.get(gname);
                    if (rmday || sw != null && (sw - 24*60*60*1000 < 23) ) {
                        unmets.remove(i);
                        //only remind weekly and monthly goals every other day
                    } else {
                        seen.put(gname, System.currentTimeMillis());
                    }
            }
        }

        int end = unmets.size();
        if (end>0) {

            String title = end + " todo item" + (end>1?"s":"");
            String text = "";

            if (end > 2) end = 2;
            for (int i = 0; i < end; i++) {
                Entry entry = unmets.get(i);
                text += entry.getGoal().getName();
                if (i < end - 1) text += ", ";
            }
            if (end < unmets.size()) text += "...";

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.goaltender)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setAutoCancel(true);

            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(notificationID, mBuilder.build());
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
