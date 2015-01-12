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
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link IntentService} subclass for polling Jenkins servers supporting Leeroy.
 */
public class LeeroyUpdateService extends IntentService {
    public static final int NOTIFICATION_UPDATE = 101;
    public static final int NOTIFICATION_ERROR = 102;

    /**
     * Checks the Jenkins URLs for updates, displaying a notification if {@link #EXTRA_NOTIFY} is
     * set. Informs the {@link ResultReceiver} in {@link #EXTRA_RECEIVER} of available updates
     * in the bundle's {@link #EXTRA_UPDATE_LIST}, apps with no updates in
     * {@link #EXTRA_NO_UPDATE_LIST} and of failed queries in {@link #EXTRA_EXCEPTION_LIST}.
     */
    public static final String ACTION_CHECK_UPDATES = "com.morlunk.leeroy.action.CHECK_UPDATES";

    public static final String EXTRA_NOTIFY = "com.morlunk.leeroy.extra.NOTIFY";
    public static final String EXTRA_UPDATE_LIST = "com.morlunk.leeroy.extra.UPDATE_LIST";
    public static final String EXTRA_NO_UPDATE_LIST = "com.morlunk.leeroy.extra.NO_UPDATE_LIST";
    public static final String EXTRA_EXCEPTION_LIST = "com.morlunk.leeroy.extra.EXCEPTION_LIST";
    public static final String EXTRA_RECEIVER = "com.morlunk.leeroy.extra.RECEIVER";

    public LeeroyUpdateService() {
        super("LeeroyUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            if (ACTION_CHECK_UPDATES.equals(action)) {
                final boolean notify = intent.getBooleanExtra(EXTRA_NOTIFY, false);
                handleCheckUpdates(intent, notify, receiver);
            }
        }
    }

    private void handleCheckUpdates(Intent intent, boolean notify, ResultReceiver receiver) {
        List<LeeroyApp> appList = LeeroyApp.getApps(getPackageManager());

        if (appList.size() == 0) {
            return;
        }

        List<LeeroyAppUpdate> updates = new LinkedList<>();
        List<LeeroyApp> notUpdatedApps = new LinkedList<>();
        List<LeeroyException> exceptions  = new LinkedList<>();
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
                    } else {
                        throw new RuntimeException("Unknown key " + name);
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
                } else {
                    notUpdatedApps.add(app);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                CharSequence appName = app.getApplicationInfo().loadLabel(getPackageManager());
                exceptions.add(new LeeroyException(app, getString(R.string.invalid_url, appName), e));
            } catch (IOException e) {
                e.printStackTrace();
                exceptions.add(new LeeroyException(app, e));
            }
        }

        if (notify) {
            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            if (updates.size() > 0) {
                NotificationCompat.Builder ncb = new NotificationCompat.Builder(this);
                ncb.setSmallIcon(R.drawable.ic_stat_update);
                ncb.setTicker(getString(R.string.updates_available));
                ncb.setContentTitle(getString(R.string.updates_available));
                ncb.setContentText(getString(R.string.num_updates, updates.size()));
                ncb.setPriority(NotificationCompat.PRIORITY_LOW);
                ncb.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                Intent appIntent = new Intent(this, AppListActivity.class);
                appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ncb.setContentIntent(PendingIntent.getActivity(this, 0, appIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
                ncb.setAutoCancel(true);
                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                for (LeeroyAppUpdate update : updates) {
                    CharSequence appName = update.app.getApplicationInfo().loadLabel(getPackageManager());
                    style.addLine(getString(R.string.notify_app_update, appName, update.app.getJenkinsBuild(),
                            update.newBuild));
                }
                style.setSummaryText(getString(R.string.app_name));
                ncb.setStyle(style);
                ncb.setNumber(updates.size());
                nm.notify(NOTIFICATION_UPDATE, ncb.build());
            }

            if (exceptions.size() > 0) {
                NotificationCompat.Builder ncb = new NotificationCompat.Builder(this);
                ncb.setSmallIcon(R.drawable.ic_stat_error);
                ncb.setTicker(getString(R.string.error_checking_updates));
                ncb.setContentTitle(getString(R.string.error_checking_updates));
                ncb.setContentText(getString(R.string.click_to_retry));
                ncb.setPriority(NotificationCompat.PRIORITY_LOW);
                ncb.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                ncb.setContentIntent(PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
                ncb.setAutoCancel(true);
                ncb.setNumber(exceptions.size());
                nm.notify(NOTIFICATION_ERROR, ncb.build());
            }
        }

        if (receiver != null) {
            Bundle results = new Bundle();
            results.putParcelableArrayList(EXTRA_UPDATE_LIST, new ArrayList<>(updates));
            results.putParcelableArrayList(EXTRA_NO_UPDATE_LIST, new ArrayList<>(notUpdatedApps));
            results.putParcelableArrayList(EXTRA_EXCEPTION_LIST, new ArrayList<>(exceptions));
            receiver.send(0, results);
        }
    }

}
