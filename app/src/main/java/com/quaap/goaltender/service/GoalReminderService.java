package com.quaap.goaltender.service;

/**
 * Created by tom on 11/4/16.
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GoalReminderService extends Service
{
    Alarm alarm = new Alarm();
    public void onCreate()
    {
        super.onCreate();
        //Toast.makeText(this, "AutoStart created!", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Toast.makeText(this, "AutoStart started!", Toast.LENGTH_LONG).show();

        alarm.setAlarm(this);
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
