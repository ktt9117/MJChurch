package org.mukdongjeil.mjchurch.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.io.IOException;

/**
 * Created by Kim SungJoong on 2015-08-17.
 */
public class MediaService extends Service {
    private static final String TAG = MediaService.class.getSimpleName();

    private static final int MEDIA_SERVICE_ID = 101;

    private final LocalBinder mBinder = new LocalBinder();

    private SermonItem mCurrentItem;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public MediaService getService() {
            return MediaService.this;
        }
    }

    private MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i(TAG, "onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            Logger.i(TAG, "mediaService intent action : " + action);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
    }

    public void startPlayer(SermonItem item) throws IOException {
        stopPlayer();
        mCurrentItem = item;
        startForegroundService();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDataSource(Const.BASE_URL + item.audioUrl);
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mPlayer.prepareAsync();
    }

    public void stopPlayer() {
        stopForeground(true);
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
        mCurrentItem = null;
    }

    private void startForegroundService() {
        Intent contentIntent = new Intent(this, MediaService.class);
        PendingIntent contentPending = PendingIntent.getService(this, 0, contentIntent, 0);

        Intent playIntent = new Intent(this, MediaService.class);
        playIntent.setAction("playAction");
        PendingIntent playPending = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText((mCurrentItem != null) ? mCurrentItem.title : "Preach a sermon")
                .setContentIntent(contentPending)
                .setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .addAction(android.R.drawable.ic_media_play, "Play", playPending)
                .build();

        startForeground(MEDIA_SERVICE_ID, notification);
    }
}
