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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private ProgressDialog mDialog;
    private Throwable mThrowable;

    public LeeroyAppUpdateTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mThrowable = null;
        mDialog = new ProgressDialog(mContext);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setMessage(mContext.getString(R.string.downloading));
        mDialog.setCancelable(true);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
                mDialog = null;
            }
        });
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
                    Log.e("Leeroy", "Failed to find artifact matching path regex");
                    mThrowable = new Exception(mContext.getString(R.string.error_no_artifact));
                }
            } catch (IOException e) {
                e.printStackTrace();
                mThrowable = e;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mDialog.hide();
        mDialog = null;
        if (mThrowable != null) {
            AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
            adb.setTitle(R.string.error);
            adb.setMessage(mThrowable.getLocalizedMessage());
            adb.setPositiveButton(android.R.string.ok, null);
            adb.show();
        }
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
