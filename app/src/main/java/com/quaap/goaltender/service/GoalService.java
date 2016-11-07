package com.quaap.goaltender.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


import com.quaap.goaltender.MainActivity;

import java.util.Calendar;

public class GoalService extends Service {

    private static String KEY = "whattodo";
    private static int STARTIT = 1;
    private static int NOTIFY = 2;


    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int dowhat = intent.getIntExtra(KEY, STARTIT);
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show();

        if (dowhat==NOTIFY) {
            notify(intent);
            return START_NOT_STICKY;
        } else {
            setAlarm(intent);
            return START_STICKY;
        }

       // return super.onStartCommand(intent, flags, startId);
    }


    public void notify(Intent intent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");


        Intent resultIntent = new Intent(this, MainActivity.class);
        // Stop the service when we are finished
        stopSelf();

    }

    public void setAlarm(Intent intent) {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // Request to start are service when the alarm date is upon us
        // We don't start an activity as we just want to pop up a notification into the system bar not a full activity
        Intent intent2 = new Intent(this, GoalService.class);



        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent2, 0);



        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);



        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 12*60*60*1000, pendingIntent);


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       // throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
}
