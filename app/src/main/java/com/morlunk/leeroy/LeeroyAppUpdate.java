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

import android.os.Parcel;
import android.os.Parcelable;

/**
* Created by andrew on 09/01/15.
*/
public class LeeroyAppUpdate implements Parcelable {
    public static final Creator<LeeroyAppUpdate> CREATOR = new Creator<LeeroyAppUpdate>() {
        @Override
        public LeeroyAppUpdate createFromParcel(Parcel source) {
            LeeroyAppUpdate update = new LeeroyAppUpdate();
            update.app = source.readParcelable(getClass().getClassLoader());
            update.newBuild = source.readInt();
            update.newBuildUrl = source.readString();
            return update;
        }

        @Override
        public LeeroyAppUpdate[] newArray(int size) {
            return new LeeroyAppUpdate[size];
        }
    };
    public LeeroyApp app;
    public int newBuild;
    public String newBuildUrl;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(app, 0);
        dest.writeInt(newBuild);
        dest.writeString(newBuildUrl);
    }
}
