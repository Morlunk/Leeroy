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

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LeeroyUpdateService extends IntentService {
    public static final int NOTIFICATION_UPDATE = 1;

    // Checks the Jenkins URLs for updates, displays a notification.
    public static final String ACTION_UPDATE = "com.morlunk.leeroy.action.CHECK_UPDATES";

    public static final String EXTRA_NOTIFY = "com.morlunk.leeroy.extra.notify";

    public LeeroyUpdateService() {
        super("LeeroyUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {
                final boolean notify = intent.getBooleanExtra(EXTRA_NOTIFY, false);
                handleCheckUpdates(notify);
            }
        }
    }

    private void handleCheckUpdates(boolean notify) {

        if (notify) {
            NotificationCompat.Builder ncb = new NotificationCompat.Builder(this);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_UPDATE, ncb.build());
        }
    }
}
