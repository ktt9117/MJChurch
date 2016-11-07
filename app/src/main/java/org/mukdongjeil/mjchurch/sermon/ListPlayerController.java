package org.mukdongjeil.mjchurch.sermon;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.DownloadUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.NetworkUtil;
import org.mukdongjeil.mjchurch.service.MediaService;

import java.io.IOException;

/**
 * Created by gradler on 2016. 11. 7..
 */

public class ListPlayerController {

    private final String TAG = ListPlayerController.class.getSimpleName();

    private Context context;
    private MediaService service;
    private ServiceConnection connection;


    private RelativeLayout playerLayout;
    private ImageView btnPlayOrPause;
    private ImageView btnDownload;
    private SermonItem currentItem;

    private TextView txtTitle;
    private TextView txtAuthor;

    private boolean equalsServiceItem;

    public ListPlayerController(Context context, View containerView) {
        this.context = context;
        playerLayout = (RelativeLayout) containerView.findViewById(R.id.player_layout);
        btnDownload = (ImageView) containerView.findViewById(R.id.btn_download);
        btnPlayOrPause = (ImageView) containerView.findViewById(R.id.btn_play_or_pause);
        txtTitle = (TextView) containerView.findViewById(R.id.title);
        txtAuthor = (TextView) containerView.findViewById(R.id.preacher);
        btnDownload.setOnClickListener(onClickListener);
        btnPlayOrPause.setOnClickListener(onClickListener);
        equalsServiceItem = false;
    }

    public void setMediaService(MediaService service) {
        this.service = service;
    }

    public void setMediaServiceConnection(ServiceConnection connection) {
        this.connection = connection;
    }

    public void updatePlayerInfo(SermonItem item) {
        txtTitle.setText(item.titleWithDate);
        txtAuthor.setText(item.preacher);
        currentItem = item;
        if (service != null) {
            //현재 서비스에 등록되어 있는 아이템과 사용자가 선택한 아이템이 같은지 체크
            equalsServiceItem = service.getCurrentPlayerItem() != null && service.getCurrentPlayerItem().titleWithDate.equals(item.titleWithDate);

            if (equalsServiceItem) {
                //재생중이면 Pause 버튼으로 표현
                if (service.isPlaying()) {
                    btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                } else {
                    //재생중이 아니면 Play 버튼으로 표현
                    btnPlayOrPause.setImageResource(R.drawable.ic_play);
                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                }
            } else {
                //서비스에 등록된 아이템이 아니면 Play 버튼으로 표현
                btnPlayOrPause.setImageResource(R.drawable.ic_play);
                btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
            }
        }
    }

    public void updatePlayerControllerIfNecessary(SermonItem item) {
        if (currentItem == null) {
            updatePlayerInfo(item);
        }

        if (service.getCurrentPlayerItem() != null) {
            updatePlayerInfo(service.getCurrentPlayerItem());
        }
    }

    public void hidePlayerController() {
        playerLayout.setVisibility(View.GONE);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentItem == null) {
                Toast.makeText(context, "설교가 선택되지 않았습니다. 목록에서 설교를 선택해주세요.", Toast.LENGTH_LONG).show();
                return;
            }

            if (v.getId() == R.id.btn_play_or_pause) {
                if (service == null) {
                    Intent service = new Intent(context, MediaService.class);
                    context.bindService(service, connection, Context.BIND_AUTO_CREATE);
                    Toast.makeText(context, "플레이어가 아직 준비중입니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                    return;
                }

                equalsServiceItem = service.getCurrentPlayerItem() != null && service.getCurrentPlayerItem().titleWithDate.equals(currentItem.titleWithDate);

                if (v.getTag() != null) {
                    int playerStatus = (int) v.getTag();
                    Logger.i(TAG, "playerStatus : " + playerStatus);
                    Logger.i(TAG, "equalsServiceItem : " + equalsServiceItem);
                    if (playerStatus == MediaService.PLAYER_STATUS_PAUSE) {
                        if (service.pausePlayer()) {
                            btnPlayOrPause.setImageResource(R.drawable.ic_play);
                            btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                        }
                    } else if (playerStatus == MediaService.PLAYER_STATUS_PLAY) {
                        if (equalsServiceItem && service.resumePlayer()) {
                            Logger.i(TAG, "equalsServiceItem == true && resumeResult == true");
                            btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                            btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                        } else {
                            Logger.i(TAG, "resumeResult == false maybe");
                            if (currentItem.downloadStatus == SermonItem.DownloadStatus.DOWNLOAD_SUCCESS) {
                                //다운로드 된 아이템은 그냥 재생한다.
                                try {
                                    service.startPlayer(currentItem);
                                    btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                    return;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            //다운로드 되지 않은 아이템은 네트워크에 연결되어 있는지 체크
                            if (NetworkUtil.getNetwork(context) == NetworkUtil.NETWORK_NONE) {
                                Toast.makeText(context, "네트워크에 연결되어 있지 않습니다. 와이파이 또는 데이터 네트워크 연결 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (!NetworkUtil.isWifi(context)) {
                                //와이파이에 연결되지 않은 상태에서 재생을 누르면 경고 문구를 보여준다.
                                if (context instanceof MainActivity) {
                                    ((MainActivity) context).showNetworkAlertDialog("와이파이에 연결되어 있지 않습니다. 이대로 진행할 경우 가입하신 요금제에 따라 추가 요금이 부과될 수도 있습니다.\n(다운로드 하거나 와이파이에서 재생하기를 권장합니다.)\n 재생 하시겠습니까?",
                                            new MainActivity.NetworkAlertResultListener() {
                                                @Override
                                                public void onClick(boolean positiveButtonClick) {
                                                    if (positiveButtonClick) {
                                                        try {
                                                            service.startPlayer(currentItem);
                                                            btnPlayOrPause.setImageResource(R.drawable.ic_pause);
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
                                    service.startPlayer(currentItem);
                                    btnPlayOrPause.setImageResource(R.drawable.ic_pause);
                                    btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PAUSE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if (playerStatus == MediaService.PLAYER_STATUS_STOP) {
                        service.stopPlayer();
                        btnPlayOrPause.setImageResource(R.drawable.ic_play);
                        btnPlayOrPause.setTag(MediaService.PLAYER_STATUS_PLAY);
                    }
                }
            } else if (v.getId() == R.id.btn_download) {
                if (DownloadUtil.isNecessaryDownload(context, currentItem)) {
                    if (NetworkUtil.getNetwork(context) == NetworkUtil.NETWORK_NONE) {
                        Toast.makeText(context, "네트워크에 연결되어 있지 않습니다. 와이파이 또는 데이터 네트워크 연결 후 다시 시도해주세요", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!NetworkUtil.isWifi(context)) {
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).showNetworkAlertDialog("와이파이에 연결되어 있지 않습니다. 이대로 진행할 경우 가입하신 요금제에 따라 추가 요금이 부과될 수도 있습니다.\n(와이파이에서 다운로드를 권장합니다.)\n 다운로드 하시겠습니까?",
                                    new MainActivity.NetworkAlertResultListener() {
                                        @Override
                                        public void onClick(boolean positiveButtonClick) {
                                            if (positiveButtonClick) {
                                                DownloadUtil.requestDownload(context, currentItem);
                                            }
                                        }
                                    });
                        }
                    } else {
                        DownloadUtil.requestDownload(context, currentItem);
                    }
                } else {
                    Toast.makeText(context, "다운로드 중이거나 이미 다운로드 완료된 파일입니다.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "구현 준비중입니다.", Toast.LENGTH_LONG).show();
            }
        }
    };
}
