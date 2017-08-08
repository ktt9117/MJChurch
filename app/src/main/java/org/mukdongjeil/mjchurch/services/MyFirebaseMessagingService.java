package org.mukdongjeil.mjchurch.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.PushMessageActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.fragments.ChatFragment;
import org.mukdongjeil.mjchurch.utils.Logger;

/**
 * Created by gradler on 2016. 6. 3..
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        RemoteMessage.Notification noti = remoteMessage.getNotification();
        if (noti != null && !TextUtils.isEmpty(noti.getBody())) {
            displayNotification(noti);
        }
    }

    private void displayNotification(RemoteMessage.Notification remoteNotification) {
        Intent intent = new Intent();
        String clickAction = remoteNotification.getClickAction();
        Logger.e(TAG, "clickAction : " + clickAction);

        if (!TextUtils.isEmpty(clickAction)) {
            if (!ChatFragment.IS_CHATROOM_FOREGROUND) {
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setAction(remoteNotification.getClickAction());
                PendingIntent contentPending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(contentPending)
                        .setContentTitle(remoteNotification.getTitle())
                        .setContentText(remoteNotification.getBody())
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis());

                NotificationManager notiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notiMgr.notify(Const.NOTIFICATION_ID_CHAT, notiBuilder.build());
            }

        } else {
            intent.setClass(this, PushMessageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Const.INTENT_KEY_MESSAGE, remoteNotification.getBody());
            startActivity(intent);
        }
    }
}
