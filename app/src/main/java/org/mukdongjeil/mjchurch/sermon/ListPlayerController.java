package org.mukdongjeil.mjchurch.sermon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.util.BlurFilter;
import org.mukdongjeil.mjchurch.common.util.DownloadUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.NetworkUtil;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.service.MediaService;

import java.io.IOException;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by gradler on 2016. 11. 7..
 */

public class ListPlayerController implements View.OnClickListener {

    private final String TAG = ListPlayerController.class.getSimpleName();

    private FragmentActivity parentActivity;
    private MediaService service;
    private ServiceConnection connection;

    private RelativeLayout playerLayout;
    private ImageView btnPlayOrPause;

    private SmoothProgressBar smoothProgressBar;
    private Sermon currentItem;
    private TextView txtTitle;
    //private TextView txtAuthor;

    private boolean equalsServiceItem;
    private NetworkAlertListener listener;

    public ListPlayerController(FragmentActivity context, View containerView, NetworkAlertListener listener) {
        this.parentActivity = context;
        playerLayout = (RelativeLayout) containerView.findViewById(R.id.player_layout);
        btnPlayOrPause = (ImageView) containerView.findViewById(R.id.btn_play_or_pause);
        txtTitle = (TextView) containerView.findViewById(R.id.title);
        //txtAuthor = (TextView) containerView.findViewById(R.id.preacher);
        smoothProgressBar = (SmoothProgressBar) containerView.findViewById(R.id.smoothProgressBar);
        smoothProgressBar.progressiveStop();

        containerView.findViewById(R.id.btn_download).setOnClickListener(this);
        btnPlayOrPause.setOnClickListener(this);
        equalsServiceItem = false;
        this.listener = listener;

        setBlurBackground();
    }

    private void setBlurBackground() {
        Bitmap bgBitmap = BitmapFactory.decodeResource(parentActivity.getResources(), R.drawable.bg_player);
        Drawable d = new BitmapDrawable(parentActivity.getResources(), BlurFilter.fastblur(bgBitmap, 10));
        bgBitmap.recycle();
        ViewCompat.setBackground(playerLayout, d);
    }

    public void setMediaService(MediaService service) {
        this.service = service;
    }

    public void setMediaServiceConnection(ServiceConnection connection) {
        this.connection = connection;
    }

    public void updatePlayerInfo(final Sermon item) {
        currentItem = item;

        txtTitle.setText(currentItem.titleWithDate);
        //txtAuthor.setText(item.preacher);

        if (service != null) {
            //현재 서비스에 등록되어 있는 아이템과 사용자가 선택한 아이템이 같은지 체크
            equalsServiceItem = service.getCurrentPlayerItem() != null && service.getCurrentPlayerItem().titleWithDate.equals(currentItem.titleWithDate);

            if (equalsServiceItem) {
                //재생중이면 Pause 버튼으로 표현
                if (service.isPlaying()) {
                    smoothProgressBar.progressiveStart();
                    setPlayerControllerVisibility(View.VISIBLE);
                    btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                    smoothProgressBar.progressiveStart();
                } else {
                    //재생중이 아니면 Play 버튼으로 표현
                    smoothProgressBar.progressiveStop();
                    btnPlayOrPause.setImageResource(R.drawable.ic_play);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    smoothProgressBar.progressiveStop();
                }
            } else {
                //서비스에 등록된 아이템이 아니면 Play 버튼으로 표현
                smoothProgressBar.progressiveStop();
                btnPlayOrPause.setImageResource(R.drawable.ic_play);
                btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                smoothProgressBar.progressiveStop();
            }
        }
    }

    public void updatePlayerControllerIfNecessary(Sermon item) {
        if (item == null) {
            return;
        }

        if (currentItem == null) {
            updatePlayerInfo(item);
        }

        if (service == null) {
            Logger.e(TAG, "updatePlayerControllerIfNeccessary pass. caused by service is null");
            return;
        }

        if (service.getCurrentPlayerItem() != null) {
            updatePlayerInfo(service.getCurrentPlayerItem());
        }
    }

    public void setPlayerControllerVisibility(int visibility) {
        playerLayout.setVisibility(visibility);
    }

    private boolean isWifiConnected(boolean isDownloadAction) {
        if (NetworkUtil.getNetwork(parentActivity) == NetworkUtil.NETWORK_NONE) {
            listener.onNetworkNotConnected();
            return false;
        }

        if (NetworkUtil.isWifi(parentActivity)) {
            return true;

        } else {
            listener.onDataAlert(isDownloadAction);
            return false;
        }
    }

    public void playCurrentItem() {
        ((MainActivity) parentActivity).startPermissionCheck(Manifest.permission.READ_PHONE_STATE,
                new MainActivity.PermissionCheckResultListener() {
            @Override
            public void onResult(boolean isGranted) {
                Logger.i(TAG, "onResult isGranted : " + isGranted);
                if (isGranted) {
                    try {
                        service.startPlayer(currentItem);
                        btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                        smoothProgressBar.progressiveStart();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(parentActivity, "\"통화 상태 조회\" 권한이 없으면 재생할 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void downloadCurrentItem() {
            ((MainActivity) parentActivity).startPermissionCheck(Manifest.permission.WRITE_EXTERNAL_STORAGE, new MainActivity.PermissionCheckResultListener() {
                @Override
                public void onResult(boolean isGranted) {
                    if (isGranted) {
                        if (currentItem != null) {
                            DownloadUtil.requestDownload(parentActivity, currentItem);

                        } else {
                            Logger.e(TAG, "Cannot request download caused by there is no current selected item");
                        }

                    } else {
                        Toast.makeText(parentActivity, "\"쓰기\" 권한이 없으면 다운로드를 진행할 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    }

    @Override
    public void onClick(View view) {
        if (currentItem == null) {
            Toast.makeText(parentActivity, "설교가 선택되지 않았습니다. 목록에서 설교를 선택해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        if (view.getId() == R.id.btn_play_or_pause) {
            if (service == null) {
                Intent service = new Intent(parentActivity, MediaService.class);
                parentActivity.bindService(service, connection, Context.BIND_AUTO_CREATE);
                Toast.makeText(parentActivity, "플레이어가 아직 준비중입니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                return;
            }

            equalsServiceItem = service.getCurrentPlayerItem() != null && service.getCurrentPlayerItem().titleWithDate.equals(currentItem.titleWithDate);

            if (view.getTag() != null) {
                int playerStatus = (int) view.getTag();
                Logger.i(TAG, "playerStatus : " + playerStatus);
                Logger.i(TAG, "equalsServiceItem : " + equalsServiceItem);
                if (playerStatus == MediaService.PLAYER_STATUS_PAUSE) {
                    if (service.pausePlayer()) {
                        btnPlayOrPause.setImageResource(R.drawable.ic_play);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                        smoothProgressBar.progressiveStop();
                    }
                } else if (playerStatus == MediaService.PLAYER_STATUS_PLAY) {
                    if (equalsServiceItem && service.resumePlayer()) {
                        Logger.i(TAG, "equalsServiceItem == true && resumeResult == true");
                        btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                        smoothProgressBar.progressiveStart();
                    } else {
                        Logger.i(TAG, "resumeResult == false maybe");
                        if (DownloadUtil.isDownloadSuccessItem(parentActivity, currentItem)) {
                            //다운로드 된 아이템은 그냥 재생한다.
                            playCurrentItem();

                        } else {
                            //다운로드 되지 않은 아이템은 네트워크에 연결되어 있는지 체크
                            if (isWifiConnected(false)) {
                                //와이파이라면 그냥 재생한다.
                                playCurrentItem();
                            }
                        }
                    }
                } else if (playerStatus == MediaService.PLAYER_STATUS_STOP) {
                    service.stopPlayer();
                    btnPlayOrPause.setImageResource(R.drawable.ic_play);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    smoothProgressBar.progressiveStop();
                }
            }

        } else if (view.getId() == R.id.btn_download) {
            if (DownloadUtil.isNecessaryDownload(parentActivity, currentItem)) {
                if (isWifiConnected(true)) {
                    downloadCurrentItem();
                }
            } else {
                Toast.makeText(parentActivity, "다운로드 중이거나 이미 다운로드 완료된 파일입니다.", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(parentActivity, "구현 준비중입니다.", Toast.LENGTH_LONG).show();
        }
    }

    public interface NetworkAlertListener {
        void onNetworkNotConnected();
        void onDataAlert(boolean playContinue);
    }
}
