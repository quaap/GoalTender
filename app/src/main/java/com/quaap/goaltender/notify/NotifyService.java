package com.quaap.goaltender.notify;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.quaap.goaltender.GoalTender;
import com.quaap.goaltender.MainActivity;
import com.quaap.goaltender.R;
import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Calendar;
import java.util.List;

/**
 * Created by tom on 11/11/16.
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int cmd = intent.getIntExtra(CMD, 0);
        System.out.println("NotifyService.onStartCommand cmd=" + cmd);
        if (cmd==CMD_SETALARM) {
            setAlarm();
        } else if (cmd==CMD_KILLALARM) {
            killAlarm();
        } else if (cmd==CMD_NOTIFY) {
            if (!GoalTender.isRunning()) showNotify();
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

        killAlarm();
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        Intent notifyintent = new Intent(this.getApplicationContext(), AutoStart.class);

        notifyintent.putExtra(NotifyService.CMD, NotifyService.CMD_NOTIFY);

        alarmIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, notifyintent, 0);

        // Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);

        alarmMgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HALF_DAY, alarmIntent);
        System.out.println("alarm set");
    }

    public void killNotify() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationID);
    }

    public void showNotify() {
        GoalDB db = GoalTender.getDatabase();

        List<Entry> unmets = db.getUnmetEntries();

        if (unmets.size()>0) {

            String text = unmets.size() + " todo's: ";
            int end = unmets.size();
            if (end > 2) end = 2;
            for (int i = 0; i < end; i++) {
                Entry entry = unmets.get(i);
                text += entry.getGoal().getName();
                if (i < end - 1) text += ", ";
            }
            if (end < unmets.size()) text += "...";

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.goal_launcher)
                            .setContentTitle("ToDos")
                            .setContentText(text);

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
