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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A task to update a Leeroy app and show a progress dialog.
 * Created by andrew on 09/01/15.
 */
public class LeeroyAppUpdateTask extends AsyncTask<LeeroyAppUpdate, Void, Void> {
    private Context mContext;
    private PendingIntent mInstallerIntent;
    private ProgressDialog mDialog;

    public LeeroyAppUpdateTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setMessage(mContext.getString(R.string.downloading));
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Void doInBackground(LeeroyAppUpdate... params) {
        for (LeeroyAppUpdate update : params) {
            // Get artifact path matching regex
            Pattern pathPattern = Pattern.compile(update.app.getJenkinsArtifactPathRegex());
            try {
                String paramUrl = update.newBuildUrl +
                        "/api/json?tree=artifacts[relativePath]";
                URL url = new URL(paramUrl);
                URLConnection conn = url.openConnection();
                Reader reader = new InputStreamReader(conn.getInputStream());

                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.beginObject();
                jsonReader.nextName();
                jsonReader.beginArray();

                String matchedPath = null;
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.nextName();
                        if ("relativePath".equals(key)) {
                            String relativePath = jsonReader.nextString();
                            Matcher matcher = pathPattern.matcher(relativePath);
                            if (matcher.matches()) {
                                matchedPath = relativePath;
                                break;
                            }
                        } else {
                            throw new RuntimeException("Unknown key " + key);
                        }
                    }
                    jsonReader.endObject();
                }

                jsonReader.endArray();
                jsonReader.endObject();
                jsonReader.close();

                if (matchedPath != null) {
                    String artifactPath = update.newBuildUrl + "/artifact/" + matchedPath;
                    URL artifactUrl = new URL(artifactPath);
                    URLConnection connection = artifactUrl.openConnection();
                    mDialog.setMax(connection.getContentLength());
                    install(connection.getInputStream());
                } else {
                    // TODO handle match failing
                    Log.e("Leeroy", "Failed to find artifact matching path regex");
                }
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mDialog.hide();
        mDialog = null;
    }

    private void install(InputStream apkStream) throws IOException {
        File apkFile = File.createTempFile("leeroy-app", ".apk");
        apkFile.setReadable(true, false);
        FileOutputStream fos = new FileOutputStream(apkFile);
        final byte[] buffer = new byte[1024];
        int read;
        while ((read = apkStream.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
            mDialog.incrementProgressBy(read);
        }
        fos.flush();
        fos.close();
        Intent installIntent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        mContext.startActivity(installIntent);
        // TODO remove APK?
    }
}
