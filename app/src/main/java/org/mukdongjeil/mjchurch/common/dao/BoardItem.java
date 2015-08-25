package org.mukdongjeil.mjchurch.common.dao;

import com.google.gson.Gson;

/**
 * Created by Kim SungJoong on 2015-08-25.
 */
public class BoardItem {
    public String title;
    public String contentUrl;
    public String writer;
    public String date;
    public String bbsNo;

    public String toString() {
        return new Gson().toJson(this);
    }
}
