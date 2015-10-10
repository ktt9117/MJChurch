package org.mukdongjeil.mjchurch.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.io.IOException;

/**
 * Created by John Kim on 2015-09-29.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = RegistrationIntentService.class.getSimpleName();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // GCM Instance ID의 토큰을 가져오는 작업이 시작되면 LocalBoardcast로 GENERATING 액션을 알려 ProgressBar가 동작하도록 한다.
//        LocalBroadcastManager.getInstance(this)
//                .sendBroadcast(new Intent(QuickstartPreferences.REGISTRATION_GENERATING));

        // GCM을 위한 Instance ID를 가져온다.
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;

        synchronized (TAG) {
            // GCM 앱을 등록하고 획득한 설정파일인 google-services.json을 기반으로 SenderID를 자동으로 가져온다.
            String default_senderId = getString(R.string.gcm_defaultSenderId);
            // GCM 기본 scope는 "GCM"이다.
            String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
            // Instance ID에 해당하는 토큰을 생성하여 가져온다.
            try {
                token = instanceID.getToken(default_senderId, scope, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Logger.i(TAG, "GCM Registration Token: " + token);
        }


        // GCM Instance ID에 해당하는 토큰을 획득하면 LocalBoardcast에 COMPLETE 액션을 알린다.
        // 이때 토큰을 함께 넘겨주어서 UI에 토큰 정보를 활용할 수 있도록 했다.
//        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
//        registrationComplete.putExtra("token", token);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
