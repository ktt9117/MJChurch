package org.mukdongjeil.mjchurch.models;

import android.support.annotation.NonNull;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gradler on 22/05/2017.
 */

public class Board extends RealmObject {
    @PrimaryKey
    @NonNull
    public String contentUrl;

    public String title;
    public String writer;
    public String date;
    public String content;
}
