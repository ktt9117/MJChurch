package org.mukdongjeil.mjchurch.models;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gradler on 10/05/2017.
 */

public class ImagePageUrl extends RealmObject {

    @PrimaryKey
    @NonNull
    public String title;

    @NonNull
    public String url;

    public int type; // 1: introduce, 3: training
    public long updatedAt;

    public ImagePageUrl() {}

    public ImagePageUrl(int type, @NonNull String title, @NonNull String url) {
        this.type = type;
        this.title = title;
        this.url = url;
        updatedAt = System.currentTimeMillis();
    }
}
