package org.mukdongjeil.mjchurch.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by John Kim on 2015-09-29.
 */
public class GCMInstanceIDListenerService extends InstanceIDListenerService{
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
