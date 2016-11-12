package com.quaap.goaltender.notify;

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

public class AutoStart extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("AutoStart.onReceive");

        Intent notifyintent = new Intent(context, NotifyService.class);

        notifyintent.putExtra(NotifyService.CMD, intent.getIntExtra(NotifyService.CMD,NotifyService.CMD_SETALARM));

        //notifyintent.putExtra(NotifyService.CMD, NotifyService.CMD_SETALARM);

        context.startService(notifyintent);

    }
}