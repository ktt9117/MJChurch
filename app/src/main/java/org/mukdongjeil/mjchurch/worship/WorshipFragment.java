package org.mukdongjeil.mjchurch.worship;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestSermonsTask;
import org.mukdongjeil.mjchurch.service.MediaService;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class WorshipFragment extends ListFragment {
    private static final String TAG = WorshipFragment.class.getSimpleName();
    private int mPageNo;
    private int mWorshipType;

    private MediaService mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaService] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "[MediaService] onServiceDisconnected");
            mService = null;
        }
    };

    private WorshipListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPageNo = 1;
        Intent service = new Intent(getActivity(), MediaService.class);
        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            int selectedMenuIndex = args.getInt(MenuListFragment.SELECTED_MENU_INDEX);
            switch (selectedMenuIndex) {
                case 8:
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_AFTERNOON;
                    break;
                case 9:
                    mWorshipType = Const.WORSHIP_TYPE_WEDNESDAY;
                    break;
                case 7:
                default:
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
                    break;
            }
        }
        Logger.d(TAG, "worshipType : " + mWorshipType);

        mAdapter = new WorshipListAdapter(getActivity(), mService);
        setListAdapter(mAdapter);
        new RequestSermonsTask(mWorshipType, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj) {
                if (obj instanceof SermonItem) {
                    mAdapter.add((SermonItem)obj);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null) {
            getActivity().unbindService(mServiceConnection);
        }
    }
}
