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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of LeeroyApp items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class LeeroyAppFragment extends ListFragment {
    private OnFragmentInteractionListener mListener;
    private ResultReceiver mUpdateReceiver;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeeroyAppFragment() {
        mUpdateReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                List<LeeroyAppUpdate> updates =
                        resultData.getParcelableArrayList(LeeroyUpdateService.EXTRA_UPDATE_LIST);
                List<LeeroyApp> appsWithoutUpdates =
                        resultData.getParcelableArrayList(LeeroyUpdateService.EXTRA_NO_UPDATE_LIST);
                List<LeeroyException> errors =
                        resultData.getParcelableArrayList(LeeroyUpdateService.EXTRA_EXCEPTION_LIST);
                LeeroyAppAdapter adapter = new LeeroyAppAdapter(getActivity(), updates,
                        appsWithoutUpdates);
                setListAdapter(adapter);
                setListShown(true);

                if (errors.size() > 0) {
                    AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                    adb.setTitle(R.string.error);
                    StringBuilder sb = new StringBuilder();
                    for (LeeroyException e : errors) {
                        CharSequence appName = e.getApp().getApplicationInfo().loadLabel(
                                getActivity().getPackageManager());
                        sb.append(appName)
                                .append(": ")
                                .append(e.getLocalizedMessage())
                                .append('\n');
                    }
                    adb.setMessage(sb.toString());
                    adb.setPositiveButton(android.R.string.ok, null);
                    adb.show();
                }
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuVisibility(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.no_updates));
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onAppSelected((LeeroyAppUpdate) l.getItemAtPosition(position));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_app_list_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {
        setListShown(false);

        Intent updateIntent = new Intent(getActivity(), LeeroyUpdateService.class);
        updateIntent.setAction(LeeroyUpdateService.ACTION_CHECK_UPDATES);
        updateIntent.putExtra(LeeroyUpdateService.EXTRA_RECEIVER, mUpdateReceiver);
        getActivity().startService(updateIntent);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onAppSelected(LeeroyAppUpdate app);
    }

}
