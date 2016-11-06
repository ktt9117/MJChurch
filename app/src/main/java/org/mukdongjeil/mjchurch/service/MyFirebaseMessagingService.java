package org.mukdongjeil.mjchurch.service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.mukdongjeil.mjchurch.common.util.Logger;

/**
 * Created by gradler on 2016. 6. 3..
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Logger.d(TAG, "From: " + remoteMessage.getFrom());
        Logger.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }
}
