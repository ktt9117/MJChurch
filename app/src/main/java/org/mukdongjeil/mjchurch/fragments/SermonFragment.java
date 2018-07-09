package org.mukdongjeil.mjchurch.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.adapters.SermonListAdapter;
import org.mukdongjeil.mjchurch.models.DownloadStatus;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.protocols.RequestSermonListTask;
import org.mukdongjeil.mjchurch.services.DataService;
import org.mukdongjeil.mjchurch.services.MediaService;
import org.mukdongjeil.mjchurch.utils.DownloadUtil;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.NetworkUtil;
import org.mukdongjeil.mjchurch.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */

public class SermonFragment extends LoadingMenuBaseFragment implements SermonListAdapter.OnRowButtonClickListener {
    private static final String TAG = SermonFragment.class.getSimpleName();

    public static final String INTENT_ACTION_DOWNLOAD_COMPLETED = "intent_action_download_completed";

    private Realm mRealm;
    // TODO: currently not used value, but It needs later
    private int mPageNo;
    private int mSermonType;
    private TextView mLoadingTextView;

    private ArrayList<Sermon> mItemList;
    private SermonListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private BroadcastReceiver mDownloadCompleteReceiver;

    private final RealmChangeListener mOnRealmChangedListener = new RealmChangeListener<Realm>() {
        @Override
        public void onChange(Realm element) {
            Logger.e(TAG, "RealmChangeListener > onChange called");
            mAdapter.notifyDataSetChanged();
        }
    };

    private OnProgressChangeListener mOnProgressChangeListener = new OnProgressChangeListener() {
        @Override
        public void onProgressChanged(long downloadQueryId, int value) {
            Logger.e(TAG, "onProgressChanged value : " + value);
            Message msg = mHandler.obtainMessage();
            msg.arg1 = value;
            msg.obj = downloadQueryId;
            mHandler.sendMessage(msg);
        }
    };

    private static final int MSG_WHAT_DOWNLOAD_PROGRESS_UPDATE = 100;
    private ExHandler<SermonFragment> mHandler = new ExHandler<SermonFragment>(this) {
        @Override
        protected void handleMessage(SermonFragment reference, Message msg) {
            if (msg.what == MSG_WHAT_DOWNLOAD_PROGRESS_UPDATE) {
                if (reference.mRealm != null && !reference.mRealm.isClosed()) {
                    Sermon item = DataService.getSermonByDownloadQueryId(reference.mRealm, (Long) msg.obj);
                    if (item != null) {
                        reference.mRealm.beginTransaction();
                        item.downloadPercent = msg.arg1;
                        reference.mRealm.commitTransaction();
                    }
                }
            }
        }
    };

    public SermonFragment() {
        // Required empty public constructor
    }

    private MediaService mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaService] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
            ((MediaService.LocalBinder)service).setMediaStatusChangedListener(
                    new MediaService.MediaStatusChangedListener() {
                @Override
                public void onStatusChanged(int status, Sermon item) {
                    Logger.i(TAG, "[onStatusChanged] status : " + status + ", item : " + item);
                    updateItemStatus(status, item);
                }
            });

            if (mService != null && mService.isPlaying() && mService.getCurrentPlayerItem() != null) {
                updateItemStatus(MediaService.PLAY_STATUS_PLAY, mService.getCurrentPlayerItem());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "[MediaService] onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Intent service = new Intent(getActivity(), MediaService.class);
        getActivity().startService(service);
        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        mPageNo = 1;

        View v = inflater.inflate(R.layout.fragment_sermon, container, false);
        mLoadingTextView = (TextView) v.findViewById(R.id.list_loading_text);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String title;
        Bundle args = getArguments();
        if (args == null) {
            Logger.e(TAG, "arguments is null");
            mSermonType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
            title = getString(R.string.worship_sunday_morning);
        } else {
            mSermonType = args.getInt(Const.INTENT_KEY_WORSHIP_TYPE);
            title = args.getString(Const.INTENT_KEY_TITLE);
        }

        getActivity().setTitle(title);
        Logger.d(TAG, "mSermonType : " + mSermonType);

        mItemList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new SermonListAdapter(mItemList, this, Glide.with(this));
        mRecyclerView.setAdapter(mAdapter);
        loadSermonList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.removeChangeListener(mOnRealmChangedListener);
        mRealm.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        if (mService != null && mService.isPlaying() && mService.getCurrentPlayerItem() != null) {
            updateItemStatus(MediaService.PLAY_STATUS_PLAY, mService.getCurrentPlayerItem());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcastReceiver();
    }

    @Override
    public void onPlayClicked(final Sermon item, final int position) {
        Logger.i(TAG, "onPlayClicked : " + position);
        if (item == null) {
            Logger.e(TAG, "Cannot play sermon caused by item is null");
            return;
        }

        if (DownloadUtil.isDownloadSuccessItem(getActivity(), item)) {
            //다운로드 된 아이템은 그냥 재생한다.
            play(item);

        } else {
            //현재 재생중인 아이템이 사용자가 선택한 항목과 같은 경우
            if (mService != null && mService.getCurrentPlayerItem() != null) {
                if (mService.getCurrentPlayerItem().bbsNo == item.bbsNo) {
                    if (item.playStatus == MediaService.PLAY_STATUS_PLAY) {
                        mService.pausePlayer();
                        return;

                    } else if (item.playStatus == MediaService.PLAY_STATUS_PAUSE) {
                        mService.resumePlayer();
                        return;
                    }
                }
            }

            //다운로드 되지 않은 아이템은 네트워크에 연결되어 있는지 체크
            switch (NetworkUtil.getNetwork(getActivity())) {
                case NetworkUtil.NETWORK_NONE:
                    showNetworkNotConnectedAlert();
                    break;
                case NetworkUtil.NETWORK_WIFI:
                    play(item);
                    break;
                default:
                    showWifiAlert(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            play(item);
                        }
                    });
                    break;
            }
        }
    }

    @Override
    public void onDownloadClicked(final Sermon item, final int position) {
        Logger.i(TAG, "onDownloadClicked : " + position);
        if (item == null) {
            Logger.e(TAG, "Cannot request download caused by item is null");
            return;
        }

        if (DownloadUtil.isNecessaryDownload(getActivity(), item)) {
            switch (NetworkUtil.getNetwork(getActivity())) {
                case NetworkUtil.NETWORK_NONE:
                    showNetworkNotConnectedAlert();
                    break;
                case NetworkUtil.NETWORK_WIFI:
                    download(item, position);
                    break;
                default:
                    showWifiAlert(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            download(item, position);
                        }
                    });
                    break;
            }

        } else {
            Toast.makeText(getActivity(), R.string.already_downloaded_or_downloading,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadSermonList() {
        showActionBarProgress();

        RealmResults<Sermon> localSermonList = DataService.getSermonList(mRealm, mSermonType);
        Logger.e(TAG, "localPageUrls.size() : " + localSermonList.size());

        //로컬 DB에 저장된 목록이 없거나 24시간 이내에 서버에서 리스트 체크를 하지 않은 경우에만 서버로부터 목록을 받아온다.
        if (localSermonList.size() < 1 || !isAlreadyCheckToday(mSermonType)) {
            new RequestSermonListTask(mSermonType, localSermonList, new RequestSermonListTask.OnSermonResultListener() {
                @Override
                public void onResult(boolean hasItem, Sermon item) {
                    if (hasItem) {
                        PreferenceUtil.setWorshipListCheckTimeInMillis(mSermonType, System.currentTimeMillis());
                        DataService.insertToRealm(mRealm, item);

                        if (mItemList != null && mItemList.size() > 0) {
                            if (mItemList.get(0).bbsNo < item.bbsNo) {
                                mItemList.add(0, item);
                            } else {
                                mItemList.add(item);
                            }
                        } else {
                            mItemList.add(item);
                        }

                        mAdapter.notifyDataSetChanged();
                        mLoadingTextView.setVisibility(View.GONE);
                        hideActionBarProgressDelayed(1500);

                    } else {
                        mLoadingTextView.setVisibility(View.VISIBLE);
                        mLoadingTextView.setText(R.string.sermon_empty_message);
                        mLoadingTextView.bringToFront();
                        hideActionBarProgress();
                    }
                }
            });
        } else {
            Logger.d(TAG, "already check today");

            DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            HashSet<Long> downloadingItemSet = getDownloadingOrPendingItemSet(downloadManager);
            //Logger.e(TAG, "downloadingOrPendingItemSet size : " + downloadingItemSet.size());

            long downloadQueryId = -1;

            mRealm.beginTransaction();

            for (Sermon item : localSermonList) {
                if (downloadingItemSet.contains(item.downloadQueryId)) {
                    item.downloadStatus = DownloadStatus.START.ordinal();
                    downloadQueryId = item.downloadQueryId;
                } else {
                    if (DownloadUtil.isDownloadSuccessItem(getActivity(), item)) {
                        item.downloadStatus = DownloadStatus.COMPLETE.ordinal();

                    } else {
                        item.downloadStatus = DownloadStatus.NONE.ordinal();
                    }
                }

                mItemList.add(item);
            }

            mRealm.commitTransaction();
            mAdapter.notifyDataSetChanged();
            hideActionBarProgress();

            if (downloadQueryId > -1) {
                new DownloadThread(downloadManager, downloadQueryId, mOnProgressChangeListener).start();
            }

            if (mAdapter.getItemCount() > 0) {
                mLoadingTextView.setVisibility(View.GONE);
            } else {
                mLoadingTextView.setVisibility(View.VISIBLE);
                mLoadingTextView.setText(R.string.sermon_empty_message);
                mLoadingTextView.bringToFront();
            }
        }

        mRealm.addChangeListener(mOnRealmChangedListener);
    }

    private HashSet<Long> getDownloadingOrPendingItemSet(DownloadManager downloadManager) {
        HashSet<Long> downloadingItemSet = new HashSet<>();

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);

        Cursor cursor = downloadManager.query(query);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            downloadingItemSet.add(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
        }

        cursor.close();

        return downloadingItemSet;
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

    private void registerBroadcastReceiver() {
        if (mDownloadCompleteReceiver == null) {
            mDownloadCompleteReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Logger.e(TAG, "onReceive Intent_action_download_completed");
                    Toast.makeText(context, R.string.worship_download_complete_message, Toast.LENGTH_LONG).show();
                }
            };
        }

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mDownloadCompleteReceiver, new IntentFilter(INTENT_ACTION_DOWNLOAD_COMPLETED));
    }

    private void unregisterBroadcastReceiver() {
        if (mDownloadCompleteReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mDownloadCompleteReceiver);
            mDownloadCompleteReceiver = null;
        }
    }

    private void play(final Sermon item) {
        if (item.mediaType == Const.MEDIA_TYPE_VIDEO) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl));
            startActivity(intent);
            return;
        }

        ((MainActivity) getActivity()).startPermissionCheck(Manifest.permission.READ_PHONE_STATE,
                new MainActivity.PermissionCheckResultListener() {
            @Override
            public void onResult(boolean isGranted) {
                Logger.i(TAG, "onResult isGranted : " + isGranted);
                if (!isGranted) {
                    Toast.makeText(getActivity(), R.string.read_phone_state_permission_required,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    mService.startPlayer(item.bbsNo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void download(final Sermon item, final int position) {
        ((MainActivity) getActivity()).startPermissionCheck(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new MainActivity.PermissionCheckResultListener() {
            @Override
            public void onResult(boolean isGranted) {
                if (!isGranted) {
                    Toast.makeText(getActivity(), R.string.write_external_strage_permission_required,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                final DownloadManager downloadManager =
                        (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                final long res = DownloadUtil.requestDownload(downloadManager, item);
                if (res == -1) {
                    Toast.makeText(getActivity(), R.string.download_failed, Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getActivity(), R.string.download_start, Toast.LENGTH_LONG).show();
                mRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Sermon queryItem = DataService.getSermon(realm, item.bbsNo);
                        if (queryItem != null) {
                            queryItem.downloadPercent = 0;
                            queryItem.downloadStatus = DownloadStatus.START.ordinal();
                            queryItem.downloadQueryId = res;
                            Logger.i(TAG, "update current item's download status to START");
                        } else {
                            Logger.e(TAG, "cannot update current item's download status caused by query result is not exists");
                        }

                        realm.close();
                    }
                });

                new DownloadThread(downloadManager, item.downloadQueryId, mOnProgressChangeListener).start();
                Logger.e(TAG, "call notifyItemChanged position : " + position);
            }
        });
    }

    private void showNetworkNotConnectedAlert() {
        Toast.makeText(getActivity(), R.string.network_is_not_connected, Toast.LENGTH_LONG).show();
    }

    private void showWifiAlert(DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle("경고");
        ab.setCancelable(false);
        ab.setMessage(R.string.network_is_not_wifi);

        ab.setPositiveButton(android.R.string.ok, onClickListener);
        ab.setNegativeButton(android.R.string.cancel, null);
        ab.create().show();
    }

    private void updateItemStatus(int status, Sermon item) {
        int position = -1;

        if (mItemList != null && mAdapter != null) {
            if (item != null) {
                for (int i = 0; i < mItemList.size(); i++) {
                    Sermon sermon = mItemList.get(i);
                    if (sermon.bbsNo == item.bbsNo) {
                        mRealm.beginTransaction();
                        sermon.playStatus = status;
                        mRealm.commitTransaction();
                        mAdapter.notifyItemChanged(i);
                    } else if (sermon.playStatus == MediaService.PLAY_STATUS_PLAY) {
                        position = i;
                    }
                }
            }

            if (position > -1 && mItemList.size() > position) {
                mRealm.beginTransaction();
                Sermon sermon = mItemList.get(position);
                sermon.playStatus = MediaService.PLAY_STATUS_STOP;
                mRealm.commitTransaction();
                mAdapter.notifyItemChanged(position);
            }
        }
    }

    private interface OnProgressChangeListener {
        void onProgressChanged(long downloadQueryId, int value);
    }

    private class DownloadThread extends Thread {
        private DownloadManager downloadManager;
        private OnProgressChangeListener listener;
        private long downloadQueryId;
        private int failCount;

        public DownloadThread(DownloadManager manager, long downloadQueryId, OnProgressChangeListener listener) {
            this.downloadManager = manager;
            this.downloadQueryId = downloadQueryId;
            this.listener = listener;
            this.failCount = 0;
        }

        @Override
        public void run() {
            super.run();
            Logger.e(TAG, "downloadThread run()");
            boolean downloading = true;
            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadQueryId);
                Cursor cursor = downloadManager.query(query);
                try {
                    cursor.moveToFirst();
                    int downloadedBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int progress = (int) ((downloadedBytes * 100l) / totalBytes);
                    listener.onProgressChanged(downloadQueryId, progress);
                } catch (Exception e) {
                    e.printStackTrace();
                    failCount++;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                if (failCount > 4) {
                    Logger.i(TAG, "Download cancel becaused by cursor exception count over 5");
                    downloading = false;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}