package org.mukdongjeil.mjchurch.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.mukdongjeil.mjchurch.common.util.Logger;

/**
 * Created by John Kim on 2016-11-13.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Logger.e(TAG, "refreshToken : " + refreshedToken);
    }
}
