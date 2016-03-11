package org.mukdongjeil.mjchurch.sermon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.DownloadUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.NetworkUtil;
import org.mukdongjeil.mjchurch.database.DBManager;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestSermonsTask;
import org.mukdongjeil.mjchurch.service.MediaService;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.io.IOException;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class SermonFragment extends ListFragment {
    private static final String TAG = SermonFragment.class.getSimpleName();

    public static final String INTENT_ACTION_DOWNLOAD_COMPLETED = "intent_action_download_completed";

    private int mPageNo;
    private int mWorshipType;
    private ListPlayerController mPlayerController;
    private SermonListAdapter mAdapter;
    private MediaService mService;
    private BroadcastReceiver mBroadcast;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaService] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
            if (mPlayerController != null) {
                if (mService.getCurrentPlayerItem() != null) {
                    mPlayerController.updatePlayerInfo(mService.getCurrentPlayerItem());
                    if (mService.isPlaying()) {
                        mPlayerController.btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                        mPlayerController.btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    } else {
                        mPlayerController.btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                        mPlayerController.btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_STOP);
                    }
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
        mPlayerController = new ListPlayerController(v);
        Intent service = new Intent(getActivity(), MediaService.class);
        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

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
                case 10:
                    mWorshipType = Const.WORSHIP_TYPE_FRIDAY;
                    break;
                case 7:
                default:
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
                    break;
            }
        }
        Logger.d(TAG, "worshipType : " + mWorshipType);

        mAdapter = new SermonListAdapter(getActivity(), mService);
        setListAdapter(mAdapter);
        new RequestSermonsTask(mWorshipType, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }
                if (obj != null && obj instanceof SermonItem) {
                    mAdapter.add((SermonItem)obj);
                    updatePlayerControllerIfNecessary((SermonItem) obj);
                } else {
                    Logger.e(TAG, "obj is null or is not SermonItem at onResult");
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mAdapter.setCurrentItemSelected(position);
        mPlayerController.updatePlayerInfo(mAdapter.getItem(position));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null) {
            getActivity().unbindService(mServiceConnection);
        }
    }

    private class ListPlayerController {
        private final String TAG = ListPlayerController.class.getSimpleName();
        public ImageView btnPlayOrPause;
        public ImageView btnDownload;
        private TextView txtTitle;
        private TextView txtAuthor;
        public SermonItem currentItem;

        private boolean equalsServiceItem;

        public ListPlayerController(View containerView) {
            btnDownload = (ImageView) containerView.findViewById(R.id.btn_download);
            btnPlayOrPause = (ImageView) containerView.findViewById(R.id.btn_play_or_pause);
            txtTitle = (TextView) containerView.findViewById(R.id.title);
            txtAuthor = (TextView) containerView.findViewById(R.id.preacher);
            btnDownload.setOnClickListener(onClickListener);
            btnPlayOrPause.setOnClickListener(onClickListener);
            equalsServiceItem = false;
        }

        public void updatePlayerInfo(SermonItem item) {
            txtTitle.setText(item.title);
            txtAuthor.setText(item.preacher);
            currentItem = item;
            if (mService != null) {
                //현재 서비스에 등록되어 있는 아이템과 사용자가 선택한 아이템이 같은지 체크
                if (mService.getCurrentPlayerItem() != null && mService.getCurrentPlayerItem().title.equals(item.title)) {
                    equalsServiceItem = true;
                } else {
                    equalsServiceItem = false;
                }

                if (equalsServiceItem) {
                    //재생중이면 Pause 버튼으로 표현
                    if (mService.isPlaying()) {
                        btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                    } else {
                        //재생중이 아니면 Play 버튼으로 표현
                        btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    }
                } else {
                    //서비스에 등록된 아이템이 아니면 Play 버튼으로 표현
                    btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                }
            }
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentItem == null) {
                    Toast.makeText(getActivity(), "설교가 선택되지 않았습니다. 목록에서 설교를 선택해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (v.getId() == R.id.btn_play_or_pause) {
                    if (mService == null) {
                        Intent service = new Intent(getActivity(), MediaService.class);
                        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
                        Toast.makeText(getActivity(), "플레이어가 아직 준비중입니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (mService.getCurrentPlayerItem() != null && mService.getCurrentPlayerItem().title.equals(currentItem.title)) {
                        equalsServiceItem = true;
                    } else {
                        equalsServiceItem = false;
                    }

                    if (v.getTag() != null) {
                        int playerStatus = (int) v.getTag();
                        Logger.i(TAG, "playerStatus : " + playerStatus);
                        Logger.i(TAG, "equalsServiceItem : " + equalsServiceItem);
                        if (playerStatus == MediaService.PLAYER_STATUS_PAUSE) {
                            if (mService.pausePlayer()) {
                                btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                                btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                            }
                        } else if (playerStatus == MediaService.PLAYER_STATUS_PLAY) {
                            if (equalsServiceItem && mService.resumePlayer()) {
                                Logger.i(TAG, "equalsServiceItem == true && resumeResult == true");
                                btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                                btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                            } else {
                                Logger.i(TAG, "resumeResult == false maybe");
                                if (currentItem.downloadStatus == SermonItem.DownloadStatus.DOWNLOAD_SUCCESS) {
                                    //다운로드 된 아이템은 그냥 재생한다.
                                    try {
                                        mService.startPlayer(currentItem);
                                        btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                        return;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //다운로드 되지 않은 아이템은 네트워크에 연결되어 있는지 체크
                                if (NetworkUtil.getNetwork(getActivity()) == NetworkUtil.NETWORK_NONE) {
                                    Toast.makeText(getActivity(), "네트워크에 연결되어 있지 않습니다. 와이파이 또는 데이터 네트워크 연결 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (!NetworkUtil.isWifi(getActivity())) {
                                    //와이파이에 연결되지 않은 상태에서 재생을 누르면 경고 문구를 보여준다.
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).showNetworkAlertDialog("와이파이에 연결되어 있지 않습니다. 이대로 진행할 경우 가입하신 요금제에 따라 추가 요금이 부과될 수도 있습니다.\n(다운로드 하거나 와이파이에서 재생하기를 권장합니다.)\n 재생 하시겠습니까?",
                                        new MainActivity.NetworkAlertResultListener() {
                                            @Override
                                            public void onClick(boolean positiveButtonClick) {
                                                if (positiveButtonClick) {
                                                    try {
                                                        mService.startPlayer(currentItem);
                                                        btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                                                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    //와이파이라면 그냥 재생한다.
                                    try {
                                        mService.startPlayer(currentItem);
                                        btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else if (playerStatus == MediaService.PLAYER_STATUS_STOP) {
                            mService.stopPlayer();
                            btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                            btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                        }
                    }
                } else if (v.getId() == R.id.btn_download) {
                    if (DownloadUtil.isNecessaryDownload(getActivity(), currentItem)) {
                        if (NetworkUtil.getNetwork(getActivity()) == NetworkUtil.NETWORK_NONE) {
                            Toast.makeText(getActivity(), "네트워크에 연결되어 있지 않습니다. 와이파이 또는 데이터 네트워크 연결 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!NetworkUtil.isWifi(getActivity())) {
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).showNetworkAlertDialog("와이파이에 연결되어 있지 않습니다. 이대로 진행할 경우 가입하신 요금제에 따라 추가 요금이 부과될 수도 있습니다.\n(와이파이에서 다운로드를 권장합니다.)\n 다운로드 하시겠습니까?",
                                new MainActivity.NetworkAlertResultListener() {
                                    @Override
                                    public void onClick(boolean positiveButtonClick) {
                                        if (positiveButtonClick) {
                                            DownloadUtil.requestDownload(getActivity(), currentItem);
                                        }
                                    }
                                });
                            }
                        } else {
                            DownloadUtil.requestDownload(getActivity(), currentItem);
                        }
                    } else {
                        Toast.makeText(getActivity(), "다운로드 중이거나 이미 다운로드 완료된 파일입니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "구현 준비중입니다.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private void updatePlayerControllerIfNecessary(SermonItem item) {
        if (mPlayerController.currentItem == null) {
            mPlayerController.updatePlayerInfo(item);
        }

        if (mService.getCurrentPlayerItem() != null) {
            mPlayerController.updatePlayerInfo(mService.getCurrentPlayerItem());
            if (mService.isPlaying()) {
                mPlayerController.btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                mPlayerController.btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
            } else {
                mPlayerController.btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                mPlayerController.btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_STOP);
            }
        }
    }

    private void registerBroadcastReceiver() {
        if (mBroadcast == null) {
            mBroadcast = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //TODO : item download done. so refresh list
                    Logger.e(TAG, "onReceive Intent_action_download_completed");
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