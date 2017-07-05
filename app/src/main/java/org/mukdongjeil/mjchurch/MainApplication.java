package org.mukdongjeil.mjchurch;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.common.util.PreferenceUtil;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by John Kim on 2016-02-11.
 */
public class MainApplication extends Application {

    public static int REQUEST_FAIL_COUNT;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        PreferenceUtil.init(getApplicationContext());
        REQUEST_FAIL_COUNT = 0;
    }

    public static void serverDownProcess(Context context) {
        Toast.makeText(context, "서버 상태가 원활하지 않아 앱을 종료합니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
        System.exit(0);
    }
}