package org.mukdongjeil.mjchurch.sermon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.PreferenceUtil;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.database.DBManager;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestSermonsTask;
import org.mukdongjeil.mjchurch.service.MediaService;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class SermonFragment extends Fragment {
    private static final String TAG = SermonFragment.class.getSimpleName();

    public static final String INTENT_ACTION_DOWNLOAD_COMPLETED = "intent_action_download_completed";

    private int mPageNo;
    private int mWorshipType;
    private ListPlayerController mPlayerController;
    private TextView listText;

    private MediaService mService;
    private BroadcastReceiver mBroadcast;

    private ArrayList<SermonItem> mItemList;
    private SermonListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean isItemAdded = false;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaService] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
            if (mPlayerController != null) {
                mPlayerController.setMediaService(mService);
                if (mService.getCurrentPlayerItem() != null) {
                    mPlayerController.updatePlayerInfo(mService.getCurrentPlayerItem());
                }
            } else {
                Logger.e(TAG, "cannot set ListPlayerController info caused by ListPlayerController is not initialized yet!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "[MediaService] onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPageNo = 1;
        View v = inflater.inflate(R.layout.fragment_worship, null);
        listText = (TextView) v.findViewById(R.id.list_text);
        mPlayerController = new ListPlayerController(getActivity(), v);
        mPlayerController.setMediaServiceConnection(mServiceConnection);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        Intent service = new Intent(getActivity(), MediaService.class);
        getActivity().startService(service);
        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        String title;

        Bundle args = getArguments();

        if (args == null) {
            Logger.e(TAG, "arguments is null");
            mWorshipType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
            title = MenuListFragment.WORSHIP_MENUS[0];
        } else {
            int selectedMenuIndex = args.getInt(MenuListFragment.SELECTED_MENU_INDEX);
            switch (selectedMenuIndex) {
                case 8:
//                    Answers.getInstance().logContentView(new ContentViewEvent()
//                            .putContentName("설교 목록 조회")
//                            .putContentType("목록 조회")
//                            .putContentId("주일 오후 설교"));
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_AFTERNOON;
                    title = MenuListFragment.WORSHIP_MENUS[1];
                    break;
                case 9:
//                    Answers.getInstance().logContentView(new ContentViewEvent()
//                            .putContentName("설교 목록 조회")
//                            .putContentType("목록 조회")
//                            .putContentId("수요 예배 설교"));
                    mWorshipType = Const.WORSHIP_TYPE_WEDNESDAY;
                    title = MenuListFragment.WORSHIP_MENUS[2];
                    break;
                case 10:
//                    Answers.getInstance().logContentView(new ContentViewEvent()
//                            .putContentName("설교 목록 조회")
//                            .putContentType("목록 조회")
//                            .putContentId("금요 기도회 설교"));
                    mWorshipType = Const.WORSHIP_TYPE_FRIDAY;
                    title = MenuListFragment.WORSHIP_MENUS[3];
                    break;
                case 7:
                default:
//                    Answers.getInstance().logContentView(new ContentViewEvent()
//                            .putContentName("설교 목록 조회")
//                            .putContentType("목록 조회")
//                            .putContentId("주일 오전 설교"));
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
                    title = MenuListFragment.WORSHIP_MENUS[0];
                    break;
            }
        }
        Logger.d(TAG, "worshipType : " + mWorshipType);
        getActivity().setTitle(title);

        mItemList = new ArrayList<>();
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SermonListAdapter(getActivity(), mService, mItemList, new SermonListAdapter.OnSermonItemClickListener() {
            @Override
            public void onSermonItemClicked(SermonItem item) {
                mPlayerController.updatePlayerInfo(item);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        loadSermonList();
    }

    private void loadSermonList() {
        //        List<SermonItem> localSermonList = DBManager.getInstance(SystemHelpers.getApplicationContext()).getSermonList(mWorshipType);
        //로컬 DB에 저장된 목록이 없거나 오늘 서버에서 리스트 체크를 하지 않은 경우에만 서버로부터 목록을 받아온다.
        //TODO : 하루 한번만 체크했을 때 이상하게 마지막 아이템을 불러오지 못하는 현상이 있다...
//        if ((localSermonList != null && localSermonList.size() < 1) || isAlreadyCheckToday(mWorshipType) == false) {
        new RequestSermonsTask(mWorshipType, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                PreferenceUtil.setWorshipListCheckTimeInMillis(mWorshipType, System.currentTimeMillis());
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }
                if (obj != null && obj instanceof SermonItem) {
                    SermonItem item = (SermonItem) obj;
                    mItemList.add(item);
                    mAdapter.notifyDataSetChanged();
                    mPlayerController.updatePlayerControllerIfNecessary(item);

                    if (isItemAdded == false) {
                        isItemAdded = true;
                        listText.setVisibility(View.GONE);
                    }
                } else {
                    Logger.e(TAG, "obj is null or is not SermonItem at onResult");
                }
            }
        }, new RequestBaseTask.OnResultNoneListener() {
            @Override
            public void onResultNone() {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }
                mPlayerController.hidePlayerController();
                listText.setVisibility(View.VISIBLE);
                listText.setText(R.string.sermon_empty_message);
                listText.bringToFront();
            }
        });
//        } else {
//            Logger.i(TAG, "already check today");
//            if (getActivity() instanceof MainActivity) {
//                ((MainActivity) getActivity()).hideLoadingDialog();
//            }
//            for (SermonItem item : localSermonList) {
//                mAdapter.add(item);
//            }
//            mAdapter.notifyDataSetChanged();
//        }
    }

    //설교 목록은 하루에 한번만 체크한다. 오늘 체크한 기록이 있다면 다시 체크하지 않기 위한 작업
    private boolean isAlreadyCheckToday(int worshipType) {
        long lastCheckTimeInMillis = PreferenceUtil.getWorshipListCheckTimeInMillis(worshipType);
        if (lastCheckTimeInMillis > 0) {
            Calendar todayCal = Calendar.getInstance();
            Calendar somedayCal = Calendar.getInstance();
            somedayCal.setTimeInMillis(lastCheckTimeInMillis);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            final String today = sdf.format(todayCal.getTime());
            final String checkedDay = sdf.format(somedayCal.getTime());

            Logger.i(TAG, "today : " + today + ", checkedDay : " + checkedDay);
            if (today.equals(checkedDay)) {
                return true;
            }
        }

        return false;
    }

    //TODO : Check this out
//    @Override
//    public void onListItemClick(ListView l, View v, int position, long id) {
//        super.onListItemClick(l, v, position, id);
//        mAdapter.setCurrentItemSelected(position);
//        mPlayerController.updatePlayerInfo(mAdapter.getItem(position));
//    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnection);
    }

    private void registerBroadcastReceiver() {
        if (mBroadcast == null) {
            mBroadcast = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Logger.e(TAG, "onReceive Intent_action_download_completed");
                    if (mItemList == null) {
                        return;
                    }

                    mItemList.clear();
                    List<SermonItem> list = DBManager.getInstance(SystemHelpers.getApplicationContext()).getSermonList(mWorshipType);
                    mItemList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                }
            };
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcast, new IntentFilter(INTENT_ACTION_DOWNLOAD_COMPLETED));
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcast != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcast);
            mBroadcast = null;
        }
    }

}