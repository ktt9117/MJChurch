package org.mukdongjeil.mjchurch.services;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.mukdongjeil.mjchurch.PushMessageActivity;
import org.mukdongjeil.mjchurch.utils.Logger;

/**
 * Created by gradler on 2016. 6. 3..
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Logger.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        Logger.e(TAG, "Notification Custome Message : " + remoteMessage.getData().get("message"));

        Intent intent = new Intent(this, PushMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("message", remoteMessage.getNotification().getBody());
        startActivity(intent);

    }
}
