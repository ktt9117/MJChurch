package org.mukdongjeil.mjchurch.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.adapters.SermonListAdapter;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.PreferenceUtil;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.protocol.RequestSermonListTask;
import org.mukdongjeil.mjchurch.service.DataService;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class SermonFragment extends BaseFragment {
    private static final String TAG = SermonFragment.class.getSimpleName();

    public static final String INTENT_ACTION_DOWNLOAD_COMPLETED = "intent_action_download_completed";

    private Realm mRealm;
    // TODO: currently not used value, but It needs later
    private int mPageNo;
    private int mSermonType;
    private TextView listLoadingText;

    private ArrayList<Sermon> mItemList;
    private SermonListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private boolean isItemAdded = false;

    private BroadcastReceiver mDownloadCompleteReceiver;

    private SermonPagerFragment.SermonSelectedListener mSermonSelectedListener;

    private final RealmChangeListener onRealmChangedListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm element) {
            Logger.e(TAG, "RealmChangeListener > onChange called");
            mAdapter.notifyDataSetChanged();
        }
    };

    public SermonFragment() {
        // Required empty public constructor
    }

    public void setSermonSelectedListener(SermonPagerFragment.SermonSelectedListener listener) {
        this.mSermonSelectedListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        mPageNo = 1;

        View v = inflater.inflate(R.layout.fragment_sermon, container, false);
        listLoadingText = (TextView) v.findViewById(R.id.list_loading_text);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            Logger.e(TAG, "arguments is null");
            mSermonType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
        } else {
            int selectedMenuIndex = args.getInt(Const.INTENT_KEY_SELECTED_MENU_INDEX);
            switch (selectedMenuIndex) {
                case 8:
                    mSermonType = Const.WORSHIP_TYPE_SUNDAY_AFTERNOON;
                    break;
                case 9:
                    mSermonType = Const.WORSHIP_TYPE_WEDNESDAY;
                    break;
                case 10:
                    mSermonType = Const.WORSHIP_TYPE_FRIDAY;
                    break;
                case 7:
                default:
                    mSermonType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
                    break;
            }
        }

        Logger.d(TAG, "mSermonType : " + mSermonType);

        mItemList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new SermonListAdapter(mItemList, new SermonListAdapter.OnSermonClickListener() {
            @Override
            public void onSermonClicked(Sermon item) {
                mSermonSelectedListener.onItemSelected(item);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        loadSermonList();
    }

    private void loadSermonList() {
        RealmResults<Sermon> localSermonList = DataService.getSermonList(mRealm, mSermonType);

        mRealm.addChangeListener(onRealmChangedListener);

//        RealmChangeListener<RealmResults<Sermon>> changeListener = new RealmChangeListener<RealmResults<Sermon>>() {
//            @Override
//            public void onChange(RealmResults<Sermon> element) {
//                mAdapter.notifyDataSetChanged();
//            }
//        };
//        mRealm.addChangeListener(changeListener);

        Logger.e(TAG, "localPageUrls.size() : " + localSermonList.size());

        //로컬 DB에 저장된 목록이 없거나 24시간 이내에 서버에서 리스트 체크를 하지 않은 경우에만 서버로부터 목록을 받아온다.
        if (localSermonList.size() < 1 || !isAlreadyCheckToday(mSermonType)) {
            new RequestSermonListTask(mSermonType, localSermonList, new RequestSermonListTask.OnSermonResultListener() {
                @Override
                public void onResult(boolean hasItem, Sermon item) {
                    if (hasItem) {
                        PreferenceUtil.setWorshipListCheckTimeInMillis(mSermonType, System.currentTimeMillis());
                        DataService.insertToRealm(mRealm, item);

                        mItemList.add(item);
                        mAdapter.notifyDataSetChanged();
                        listLoadingText.setVisibility(View.GONE);

                    } else {
                        listLoadingText.setVisibility(View.VISIBLE);
                        listLoadingText.setText(R.string.sermon_empty_message);
                        listLoadingText.bringToFront();
                    }
                }
            });
        } else {
            Logger.d(TAG, "already check today");
            for (Sermon item : localSermonList) {
                mItemList.add(item);
            }

            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 0) {
                listLoadingText.setVisibility(View.GONE);
            } else {
                listLoadingText.setVisibility(View.VISIBLE);
                listLoadingText.setText(R.string.sermon_empty_message);
                listLoadingText.bringToFront();
            }
        }
    }

    //설교 목록은 하루에 한번만 체크한다. 24시간 이내에 서버값을 체크한 기록이 있다면 다시 체크하지 않기 위한 작업
    private boolean isAlreadyCheckToday(int worshipType) {
        long lastCheckTimeInMillis = PreferenceUtil.getWorshipListCheckTimeInMillis(worshipType);
        if (lastCheckTimeInMillis > 0) {
            long oneDayGap = System.currentTimeMillis() - Const.DAY_IN_MILLIS;
            return lastCheckTimeInMillis > oneDayGap;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.removeChangeListener(onRealmChangedListener);
        mRealm.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        if (mDownloadCompleteReceiver == null) {
            mDownloadCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Logger.e(TAG, "onReceive Intent_action_download_completed");
                    mAdapter.notifyDataSetChanged();
                }
            };
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDownloadCompleteReceiver, new IntentFilter(INTENT_ACTION_DOWNLOAD_COMPLETED));
    }

    private void unregisterBroadcastReceiver() {
        if (mDownloadCompleteReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDownloadCompleteReceiver);
            mDownloadCompleteReceiver = null;
        }
    }
}