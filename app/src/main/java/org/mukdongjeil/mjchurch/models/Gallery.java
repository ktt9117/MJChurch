package org.mukdongjeil.mjchurch.models;

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
    public String photoUrl;
}
