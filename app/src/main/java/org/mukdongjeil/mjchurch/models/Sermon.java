package org.mukdongjeil.mjchurch.models;

import com.google.gson.Gson;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gradler on 15/05/2017.
 */

public class Sermon extends RealmObject {

    @PrimaryKey
    public String bbsNo;

    public int sermonType;
    public String titleWithDate;
    public String content;
    public String contentUrl;
    public String preacher;
    public String chapterInfo;
    public String audioUrl;
    public String docUrl;
    public long downloadQueryId;
    public int downloadStatus;

    public Sermon() {
        downloadQueryId = -1;
        downloadStatus = 0;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
