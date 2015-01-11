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
 * Represents a failed Jenkins query for a {@link com.morlunk.leeroy.LeeroyApp}.
 * Created by andrew on 10/01/15.
 */
public class LeeroyException extends Exception implements Parcelable {
    public static final Creator<LeeroyException> CREATOR = new Creator<LeeroyException>() {
        @Override
        public LeeroyException createFromParcel(Parcel source) {
            return new LeeroyException(source);
        }

        @Override
        public LeeroyException[] newArray(int size) {
            return new LeeroyException[size];
        }
    };

    private LeeroyApp mApp;

    public LeeroyException(LeeroyApp app, String message, Exception e) {
        super(message, e);
        mApp = app;
    }

    public LeeroyException(LeeroyApp app, Exception e) {
        super(e);
        mApp = app;
    }

    private LeeroyException(Parcel in) {
        super((Throwable) in.readSerializable());
        mApp = in.readParcelable(getClass().getClassLoader());
    }

    public LeeroyApp getApp() {
        return mApp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
        dest.writeParcelable(mApp, 0);
    }
}
