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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by andrew on 08/01/15.
 */
public class LeeroyAppAdapter extends BaseAdapter {
    private Context mContext;
    private List<LeeroyAppUpdate> mAppList;

    public LeeroyAppAdapter(Context context, List<LeeroyAppUpdate> appList) {
        mContext = context;
        mAppList = appList;
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(R.layout.item_app, parent, false);
        }
        final LeeroyApp app = mAppList.get(position).app;
        final ImageView icon = (ImageView) v.findViewById(R.id.item_app_icon);
        final TextView title = (TextView) v.findViewById(R.id.item_app_title);
        final TextView version = (TextView) v.findViewById(R.id.item_app_version);
        icon.setImageDrawable(app.getApplicationInfo().loadIcon(mContext.getPackageManager()));
        title.setText(app.getApplicationInfo().loadLabel(mContext.getPackageManager()));
        version.setText(mContext.getString(R.string.installed_build, app.getJenkinsBuild()));
        return v;
    }
}
