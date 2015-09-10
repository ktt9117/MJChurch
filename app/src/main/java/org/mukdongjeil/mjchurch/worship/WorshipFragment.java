package org.mukdongjeil.mjchurch.worship;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import org.mukdongjeil.mjchurch.common.util.StringUtils;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestSermonsTask;
import org.mukdongjeil.mjchurch.service.MediaService;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class WorshipFragment extends ListFragment {
    private static final String TAG = WorshipFragment.class.getSimpleName();
    private int mPageNo;
    private int mWorshipType;
    private long mLatestDownloadRequestId;
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
            public void onResult(Object obj, int position) {
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

                // TODO : 네트워크 망 체크해서 경고 문구 추가

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
                                try {
                                    mService.startPlayer(currentItem);
                                    btnPlayOrPause.setImageResource(R.mipmap.ic_pause);
                                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (playerStatus == MediaService.PLAYER_STATUS_STOP) {
                            mService.stopPlayer();
                            btnPlayOrPause.setImageResource(R.mipmap.ic_play);
                            btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                        }
                    }
                } else if (v.getId() == R.id.btn_download) {
                    DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

                    if (checkNecessaryDownload(downloadManager, currentItem)) {
                        Uri downloadUri = Uri.parse(Const.BASE_URL + currentItem.audioUrl);
                        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                        request.setTitle("설교 다운로드 중...");
                        request.setDescription(currentItem.title);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, currentItem.title + StringUtils.FILE_EXTENSION_MP3);
                        mLatestDownloadRequestId = downloadManager.enqueue(request);
                    } else {
                        Toast.makeText(getActivity(), "다운로드 중이거나 이미 다운로드 완료된 파일입니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "구현 준비중입니다.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private boolean checkNecessaryDownload(DownloadManager downloadManager, SermonItem item) {
        boolean isNecessaryDownload = false;

        // 1. 다운로드 폴더가 없을 경우를 대비해 폴더 생성
        Const.DIR_PUB_DOWNLOAD.mkdirs();

        // 2. 다운로드 폴더에 (#title).mp3와 동일한 이름의 파일이 있는지 체크
        File file = new File(Const.DIR_PUB_DOWNLOAD, item.title + StringUtils.FILE_EXTENSION_MP3);
        if (file != null && file.exists()) {
            // 2-1. 파일이 있는 경우 : 다운로드 실패 목록중에 해당 항목이 존재하는지 체크
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterByStatus(DownloadManager.STATUS_FAILED);
            Logger.i(TAG, "=========== Download Failed Item Query ==========");
            Cursor c = downloadManager.query(query);
            if (c != null && c.getCount() > 0) {
                boolean loopFlag = true;
                while (c.moveToNext() && loopFlag) {
                    String columnUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                    Logger.i(TAG, "COLUMN_URI : " + columnUri);
                    Logger.i(TAG, "item.audioUrl : " + Const.BASE_URL + item.audioUrl);
                    // 2-1-1. 같은 항목이 있는 경우 : 이전에 요청했던 다운로드가 실패했기 때문에 다시 다운로드 요청
                    if (columnUri.equals(Const.BASE_URL + item.audioUrl)) {
                        isNecessaryDownload = true;
                        //기존에 있던 파일이 정상적으로 다운로드 되지 않은 파일이므로 삭제
                        file.delete();
                        loopFlag = false;
                    }
                }
                c.close();
            } else {
                // 2-1-2. 실패 목록이 없으면 이전에 요청했던 다운로드가 성공했고, 실제로 파일도 있으므로 중복 요청으로 간주
                Logger.i(TAG, "cursor is null or count <= 0");
            }
        } else {
            // 2-2. 파일이 없는 경우 : 다운로드 요청
            isNecessaryDownload = true;
        }

        return isNecessaryDownload;
    }
}