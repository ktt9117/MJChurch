package org.mukdongjeil.mjchurch.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.StringUtils;
import org.mukdongjeil.mjchurch.models.Sermon;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;

/**
 * Created by Kim SungJoong on 2015-08-17.
 */
public class MediaService extends Service {
    private static final String TAG = MediaService.class.getSimpleName();

    public interface MediaStatusChangedListener {
        void onStatusChanged(int status, Sermon item);
    }

    public static final int PLAY_STATUS_NONE = 0;
    public static final int PLAY_STATUS_PLAY = 1;
    public static final int PLAY_STATUS_PAUSE = 2;
    public static final int PLAY_STATUS_STOP = 3;

    private static final int MEDIA_SERVICE_ID = 101;

    private final LocalBinder mBinder = new LocalBinder();
    private Sermon mCurrentItem;
    private Realm mRealm;
    private PhoneCallStateListener mCallStateListener;
    private MediaPlayer mPlayer;
    private MediaStatusChangedListener mMediaStatusChangedListener;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public MediaService getService() {
            return MediaService.this;
        }

        public void setMediaStatusChangedListener(MediaStatusChangedListener listener) {
            mMediaStatusChangedListener = listener;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCallStateListener = new PhoneCallStateListener();
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG, "onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            Logger.i(TAG, "mediaService intent action : " + action);
            if (TextUtils.isEmpty(action)) {
                return START_STICKY;
            }

            if (action.equals("playAction")) {
                if (!resumePlayer()) {
                    try {
                        startPlayer(mCurrentItem.bbsNo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (action.equals("pauseAction")) {
                pausePlayer();
            } else if (action.equals("stopAction")) {
                stopPlayer();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy called");
        stopPlayer();
        if (mRealm != null) mRealm.close();
    }

    public void startPlayer(int bbsNo) throws IOException {
        Logger.i(TAG, "startPlayer called");

        mCurrentItem = DataService.getSermon(mRealm, bbsNo);
        if (mCurrentItem == null) {
            Logger.e(TAG, "startPlayer failed. there is no sermon in realm database");
            return;
        }

        startForegroundService(mCurrentItem.titleWithDate);
        reInitializeMediaPlayer();

        File file = new File(Const.DIR_PUB_DOWNLOAD, mCurrentItem.bbsNo + StringUtils.FILE_EXTENSION_MP3);
        if (file.exists()) {
            mPlayer.setDataSource(file.getPath());
            Logger.i(TAG, "Local File Playing...");

        } else {
            if (TextUtils.isEmpty(mCurrentItem.audioUrl)) {
                Logger.e(TAG, "Cannot play audio caused by audioUrl is empty");
                return;
            }
            String audioUri = Const.BASE_URL + mCurrentItem.audioUrl;
            mPlayer.setDataSource(audioUri);
            Logger.i(TAG, "Server File Streaming audioUri : " + audioUri);
        }

        mPlayer.prepareAsync();

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void reInitializeMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    //Logger.e(TAG, "onBufferingUpdate percent : " + percent);
                }
            });

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    if (mMediaStatusChangedListener != null) {
                        mMediaStatusChangedListener.onStatusChanged(PLAY_STATUS_PLAY, mCurrentItem);
                    }
                }
            });

            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //stopPlayer();
                    Logger.i(TAG, "onCompletion");
                    mediaPlayer.reset();
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    tm.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);
                }
            });

            mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    Toast.makeText(getApplicationContext(), "시스템 문제로 재생할 수 없습니다", Toast.LENGTH_LONG).show();
                    Logger.e(TAG, "onError : " + what + ", " + extra);
                    stopPlayer();
                    return false;
                }
            });

            mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                    Logger.e(TAG, "onInfo : " + what + ", " + extra);
                    return false;
                }
            });
        } else {
            mPlayer.reset();
        }
    }

    public void stopPlayer() {
        Logger.e(TAG, "stopPlayer called");
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }

        if (mMediaStatusChangedListener != null) {
            mMediaStatusChangedListener.onStatusChanged(PLAY_STATUS_STOP, mCurrentItem);
        }

        mCurrentItem = null;

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);
        stopForeground(true);
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public boolean pausePlayer() {
        if (mPlayer == null) {
            return false;
        }

        mPlayer.pause();
        if (mMediaStatusChangedListener != null) {
            mMediaStatusChangedListener.onStatusChanged(PLAY_STATUS_PAUSE, mCurrentItem);
        }
        return true;
    }

    public boolean resumePlayer() {
        if (mPlayer == null) {
            return false;
        }

        mPlayer.start();
        if (mMediaStatusChangedListener != null) {
            mMediaStatusChangedListener.onStatusChanged(PLAY_STATUS_PLAY, mCurrentItem);
        }

        return true;
    }

    public Sermon getCurrentPlayerItem() {
        return mCurrentItem;
    }

    private void startForegroundService(String title) {
        Intent contentIntent = new Intent(this, MediaService.class);
        PendingIntent contentPending = PendingIntent.getService(this, 0, contentIntent, 0);

        Intent playIntent = new Intent(this, MediaService.class);
        playIntent.setAction("playAction");
        PendingIntent playPending = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, MediaService.class);
        pauseIntent.setAction("pauseAction");
        PendingIntent pausePending = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, MediaService.class);
        stopIntent.setAction("stopAction");
        PendingIntent stopPending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.remote_media_player);
        remoteView.setOnClickPendingIntent(R.id.remote_play, playPending);
        remoteView.setOnClickPendingIntent(R.id.remote_pause, pausePending);
        remoteView.setOnClickPendingIntent(R.id.remote_stop, stopPending);
        remoteView.setTextViewText(R.id.remote_title, title);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentPending)
                .setContent(remoteView)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis());

        Notification notification = notiBuilder.build();

        startForeground(MEDIA_SERVICE_ID, notification);
    }

    private class PhoneCallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Logger.i(TAG, "PhoneCallStateListener onCallStateChanged state : " + state);
            super.onCallStateChanged(state, incomingNumber);
            if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                if (mPlayer != null && mPlayer.isPlaying()) {
                    pausePlayer();
                }
            }
        }
    }
}