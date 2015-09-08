package org.mukdongjeil.mjchurch.worship;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
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
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestSermonsTask;
import org.mukdongjeil.mjchurch.service.MediaService;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.io.IOException;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class WorshipFragment extends ListFragment {
    private static final String TAG = WorshipFragment.class.getSimpleName();
    private int mPageNo;
    private int mWorshipType;
    private ListPlayerController mPlayerController;

    private MediaService mService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaService] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
            if (mPlayerController != null) {
                if (mService.getCurrentPlayerItem() != null) {
                    mPlayerController.updatePlayerInfo(mService.getCurrentPlayerItem());
                    if (mService.isPlaying()) {
                        mPlayerController.btnPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
                        mPlayerController.btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    } else {
                        mPlayerController.btnPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
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

    private WorshipListAdapter mAdapter;

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
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }
                if (obj != null && obj instanceof SermonItem) {
                    mAdapter.add((SermonItem)obj);
                    if (mPlayerController.currentItem == null) {
                        mPlayerController.updatePlayerInfo((SermonItem) obj);
                    }
                } else {
                    Logger.e(TAG, "obj is null or is not SermonItem at onResult");
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mPlayerController.updatePlayerInfo(mAdapter.getItem(position));
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
        private ImageView btnNext;
        private ImageView btnPrev;
        public ImageView btnPlayOrPause;
        private TextView txtTitle;
        private TextView txtAuthor;
        public SermonItem currentItem;

        private boolean equalsServiceItem;

        public ListPlayerController(View containerView) {
            btnNext = (ImageView) containerView.findViewById(R.id.btn_next);
            btnPrev = (ImageView) containerView.findViewById(R.id.btn_previous);
            btnPlayOrPause = (ImageView) containerView.findViewById(R.id.btn_play_or_pause);
            txtTitle = (TextView) containerView.findViewById(R.id.title);
            txtAuthor = (TextView) containerView.findViewById(R.id.preacher);
            btnNext.setOnClickListener(onClickListener);
            btnPrev.setOnClickListener(onClickListener);
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
                        btnPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                    } else {
                        //재생중이 아니면 Play 버튼으로 표현
                        btnPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    }
                } else {
                    //서비스에 등록된 아이템이 아니면 Play 버튼으로 표현
                    btnPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                }
            }
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_play_or_pause) {
                    if (mService == null) {
                        Intent service = new Intent(getActivity(), MediaService.class);
                        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
                        Toast.makeText(getActivity(), "플레이어가 아직 준비중입니다.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (currentItem == null) {
                        Toast.makeText(getActivity(), "목록에서 설교를 선택해주세요.", Toast.LENGTH_LONG).show();
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
                                btnPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
                                btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                            }
                        } else if (playerStatus == MediaService.PLAYER_STATUS_PLAY) {
                            if (equalsServiceItem && mService.resumePlayer()) {
                                Logger.i(TAG, "equalsServiceItem == true && resumeResult == true");
                                btnPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
                                btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                            } else {
                                Logger.i(TAG, "resumeResult == false maybe");
                                try {
                                    mService.startPlayer(currentItem);
                                    btnPlayOrPause.setImageResource(android.R.drawable.ic_media_pause);
                                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (playerStatus == MediaService.PLAYER_STATUS_STOP) {
                            mService.stopPlayer();
                            btnPlayOrPause.setImageResource(android.R.drawable.ic_media_play);
                            btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "구현 준비중입니다.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }
}
