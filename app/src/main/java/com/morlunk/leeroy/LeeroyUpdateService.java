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
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link IntentService} subclass for polling Jenkins servers supporting Leeroy.
 */
public class LeeroyUpdateService extends IntentService {
    public static final int NOTIFICATION_UPDATE = 101;

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
        List<LeeroyApp> appList = LeeroyApp.getApps(getPackageManager());

        if (appList.size() == 0) {
            return;
        }

        List<LeeroyAppUpdate> updates = new LinkedList<>();
        for (LeeroyApp app : appList) {
            try {
                String paramUrl = app.getJenkinsUrl() +
                        "/api/json?tree=lastSuccessfulBuild[number,url]";
                URL url = new URL(paramUrl);
                URLConnection conn = url.openConnection();
                Reader reader = new InputStreamReader(conn.getInputStream());

                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.beginObject();
                jsonReader.nextName();
                jsonReader.beginObject();

                int latestSuccessfulBuild = 0;
                String buildUrl = null;
                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();
                    if ("number".equals(name)) {
                        latestSuccessfulBuild = jsonReader.nextInt();
                    } else if ("url".equals(name)) {
                        buildUrl = jsonReader.nextString();
                    }
                }
                jsonReader.endObject();
                jsonReader.endObject();
                jsonReader.close();

                if (latestSuccessfulBuild > app.getJenkinsBuild()) {
                    LeeroyAppUpdate update = new LeeroyAppUpdate();
                    update.app = app;
                    update.newBuild = latestSuccessfulBuild;
                    update.newBuildUrl = buildUrl;
                    updates.add(update);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (notify) {
            NotificationCompat.Builder ncb = new NotificationCompat.Builder(this);
            ncb.setSmallIcon(R.drawable.ic_launcher);
            ncb.setTicker(getString(R.string.updates_available));
            ncb.setContentTitle(getString(R.string.updates_available));
            ncb.setPriority(NotificationCompat.PRIORITY_LOW);
            ncb.setContentIntent(PendingIntent.getActivity(this, 0,
                    new Intent(this, AppListActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(ncb);
            for (LeeroyAppUpdate update : updates) {
                CharSequence appName = update.app.getApplicationInfo().loadLabel(getPackageManager());
                style.addLine(getString(R.string.app_update, appName, update.app.getJenkinsBuild(),
                        update.newBuild));
            }
            ncb.setNumber(updates.size());
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_UPDATE, ncb.build());
        }
    }

    public static class LeeroyAppUpdate {
        public LeeroyApp app;
        public int newBuild;
        public String newBuildUrl;
    }
}
