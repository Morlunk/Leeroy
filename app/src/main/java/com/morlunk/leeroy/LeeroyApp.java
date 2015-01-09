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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * An application that has provided Leeroy metadata.
 * Created by andrew on 08/01/15.
 */
public class LeeroyApp implements Parcelable {
    public static final Parcelable.Creator<LeeroyApp> CREATOR = new Creator<LeeroyApp>() {
        @Override
        public LeeroyApp createFromParcel(Parcel source) {
            return new LeeroyApp(source);
        }

        @Override
        public LeeroyApp[] newArray(int size) {
            return new LeeroyApp[size];
        }
    };

    public static final String LEEROY_SUPPORTED_KEY = "com.morlunk.leeroy.SUPPORTED";
    public static final String JENKINS_URL_KEY = "com.morlunk.leeroy.JENKINS_URL";
    public static final String JENKINS_BUILD_KEY = "com.morlunk.leeroy.JENKINS_BUILD";
    public static final String JENKINS_ARTIFACT_KEY = "com.morlunk.leeroy.JENKINS_ARTIFACT";

    private ApplicationInfo mApplicationInfo;

    /**
     * Finds all apps installed on the device that support Leeroy.
     * @param pm The system package manager.
     * @return A list of apps that provide Leeroy metadata.
     */
    public static ArrayList<LeeroyApp> getApps(PackageManager pm) {
        List<ApplicationInfo> infoList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<LeeroyApp> appList = new ArrayList<>();
        for (ApplicationInfo info : infoList) {
            if (isSupported(info)) {
                appList.add(new LeeroyApp(info));
            }
        }
        return appList;
    }

    /**
     * Checks to see if the given application supports Leeroy.
     * @param info ApplicationInfo describing an application.
     * @return true if Leeroy is supported.
     */
    public static boolean isSupported(ApplicationInfo info) {
        return info.metaData != null && info.metaData.getBoolean(LEEROY_SUPPORTED_KEY);
    }

    private LeeroyApp(ApplicationInfo info) {
        mApplicationInfo = info;
    }

    private LeeroyApp(Parcel in) {
        mApplicationInfo = in.readParcelable(getClass().getClassLoader());
    }

    public ApplicationInfo getApplicationInfo() {
        return mApplicationInfo;
    }

    public String getJenkinsUrl() {
        return mApplicationInfo.metaData.getString(JENKINS_URL_KEY);
    }

    public int getJenkinsBuild() {
        return mApplicationInfo.metaData.getInt(JENKINS_BUILD_KEY);
    }

    public String getJenkinsArtifactRegex() {
        return mApplicationInfo.metaData.getString(JENKINS_ARTIFACT_KEY);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mApplicationInfo, 0);
    }
}
