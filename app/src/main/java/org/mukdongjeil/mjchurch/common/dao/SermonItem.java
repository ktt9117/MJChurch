package org.mukdongjeil.mjchurch.common.dao;

import com.google.gson.Gson;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class SermonItem {
    public int _id;
    public String title;
    public String content;
    public String contentUrl;
    public String preacher;
    public String chapterInfo;
    public String date;
    public String audioUrl;
    public String docUrl;
    public String bbsNo;
    public long downloadQueryId;

    public String toString() {
        return new Gson().toJson(this);
    }
}
