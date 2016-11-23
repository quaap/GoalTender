package com.quaap.goaltender.service;

/**
 * Created by tom on 11/4/16.
 */
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.quaap.goaltender.GoalTender;
import com.quaap.goaltender.MainActivity;
import com.quaap.goaltender.R;
import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Calendar;
import java.util.List;

public class Alarm extends BroadcastReceiver
{
    public final static int notificationID = 98458;
    @Override
    public void onReceive(Context context, Intent intent)
    {
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
//        wl.acquire();

        // Put here YOUR code.
 //       Toast.makeText(context, "AutoStart !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example

//
        boolean killnotify = intent.getBooleanExtra("killnotify", false);

        if (killnotify) {
            killnotify(context);
            return;
        }

        GoalDB db = GoalTender.getDatabase();

        List<Entry> unmets = db.getUnmetEntries();

        if (unmets.size()>0) {

            String text = unmets.size() + " todo's: ";
            int end = unmets.size();
            if (end>2) end = 2;
            for (int i=0; i<end;i++) {
                Entry entry = unmets.get(i);
                text += entry.getGoal().getName();
                if (i<end-1) text += ", ";
            }
            if (end<unmets.size()) text += "...";

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.goal_launcher)
                            .setContentTitle("ToDos")
                            .setContentText(text);

            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(notificationID, mBuilder.build());

        } else {
            killnotify(context);
        }

        // wl.release();
    }

    public void killnotify(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(notificationID);
    }

    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        // Check every 12 hours, 7 am and 7 pm
        am.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 12*60*60*1000, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}