package com.quaap.goaltender.notify;

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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