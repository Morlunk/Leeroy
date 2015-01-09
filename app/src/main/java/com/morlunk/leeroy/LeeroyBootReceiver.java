/*
 * Copyright (C) 2015 Andrew Comminos <andrew@morlunk.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.morlunk.leeroy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LeeroyBootReceiver extends BroadcastReceiver {
    public LeeroyBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // TODO: read interval preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            long interval = 10; // minutes
            long intervalMs = 60 * 1000 * interval;
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent updateIntent = new Intent(context, LeeroyUpdateService.class);
            intent.setAction(LeeroyUpdateService.ACTION_UPDATE);
            intent.putExtra(LeeroyUpdateService.EXTRA_NOTIFY, true);
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, intervalMs, intervalMs,
                    PendingIntent.getService(context, 0, updateIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }
}
