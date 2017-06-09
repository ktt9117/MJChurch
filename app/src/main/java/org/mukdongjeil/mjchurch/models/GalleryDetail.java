package org.mukdongjeil.mjchurch.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by gradler on 08/06/2017.
 */

public class GalleryDetail extends RealmObject {
    @PrimaryKey
    public String contentNo;
    public RealmList<RealmString> imageUrlList;
}
