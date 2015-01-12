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
 * An adapter displaying Leeroy apps with updates, and Leeroy apps that are up to date underneath.
 * Created by andrew on 08/01/15.
 */
public class LeeroyAppAdapter extends BaseAdapter {
    private Context mContext;
    private List<LeeroyAppUpdate> mUpdateList;
    private List<LeeroyApp> mNoUpdateList;

    public LeeroyAppAdapter(Context context, List<LeeroyAppUpdate> appList,
                            List<LeeroyApp> appListWithoutUpdates) {
        mContext = context;
        mUpdateList = appList;
        mNoUpdateList = appListWithoutUpdates;
    }

    @Override
    public int getCount() {
        return mUpdateList.size() + mNoUpdateList.size();
    }

    @Override
    public Object getItem(int position) {
        if (isUpdate(position)) {
            return mUpdateList.get(position);
        } else {
            return mNoUpdateList.get(position - mUpdateList.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return isUpdate(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(R.layout.item_app, parent, false);
        }
        final ImageView icon = (ImageView) v.findViewById(R.id.item_app_icon);
        final TextView title = (TextView) v.findViewById(R.id.item_app_title);
        final TextView version = (TextView) v.findViewById(R.id.item_app_version);
        final LeeroyApp app;
        if (isUpdate(position)) {
            final LeeroyAppUpdate update = (LeeroyAppUpdate) getItem(position);
            app = update.app;
            version.setText(mContext.getString(R.string.app_update, app.getJenkinsBuild(),
                    update.newBuild));
            v.setAlpha(1.f);
        } else {
            app = (LeeroyApp) getItem(position);
            version.setText(mContext.getString(R.string.app_current, app.getJenkinsBuild()));
            v.setAlpha(0.5f);
        }
        icon.setImageDrawable(app.getApplicationInfo().loadIcon(mContext.getPackageManager()));
        title.setText(app.getApplicationInfo().loadLabel(mContext.getPackageManager()));
        return v;
    }

    private boolean isUpdate(int position) {
        return position < mUpdateList.size(); // show updates before apps.
    }
}
