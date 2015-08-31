package org.mukdongjeil.mjchurch.common.dao;

import com.google.gson.Gson;

/**
 * Created by Kim SungJoong on 2015-08-31.
 */
public class GalleryItem {
    public String title;
    public String contentUrl;
    public String date;
    public String bbsNo;
    public String photoUrl;

    public String toString() {
        return new Gson().toJson(this);
    }
}
