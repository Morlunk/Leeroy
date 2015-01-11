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
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by andrew on 09/01/15.
 */
public class LeeroyApplication extends Application implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private PendingIntent mAlarmIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Intent updateIntent = new Intent(context, LeeroyUpdateService.class);
        updateIntent.setAction(LeeroyUpdateService.ACTION_CHECK_UPDATES);
        updateIntent.putExtra(LeeroyUpdateService.EXTRA_NOTIFY, true);
        mAlarmIntent = PendingIntent.getService(context, 0, updateIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        updateAlarm(prefs);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (LeeroySettings.INTERVAL_KEY.equals(key)) {
            updateAlarm(sharedPreferences);
        }
    }

    private void updateAlarm(SharedPreferences preferences) {
        Context context = getApplicationContext();
        int interval = Integer.parseInt(preferences.getString(LeeroySettings.INTERVAL_KEY,
                LeeroySettings.DEFAULT_INTERVAL));
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(mAlarmIntent);
        if (interval > 0) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, mAlarmIntent);
        }
    }
}
