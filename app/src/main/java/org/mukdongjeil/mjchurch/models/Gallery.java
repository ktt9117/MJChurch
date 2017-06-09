package org.mukdongjeil.mjchurch.models;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Kim SungJoong on 2015-08-31.
 */
public class Gallery extends RealmObject {

    public int boardType;
    public String title;
    public String contentUrl;
    public String date;
    public String bbsNo;

    @PrimaryKey
    @NonNull
    public String photoUrl;

    public String toString() {
        return new Gson().toJson(this);
    }
}
