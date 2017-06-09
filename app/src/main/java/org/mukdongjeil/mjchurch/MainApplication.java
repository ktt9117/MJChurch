package org.mukdongjeil.mjchurch;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

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

        REQUEST_FAIL_COUNT = 0;

        PreferenceUtil.init(getApplicationContext());

        // init Universal Image Loader
        initializeImageLoader();
    }

    public static void serverDownProcess(Context context) {
        Toast.makeText(context, "서버 상태가 원활하지 않아 앱을 종료합니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
        System.exit(0);
    }

    private void initializeImageLoader() {

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .denyCacheImageMultipleSizesInMemory()
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);
    }
}