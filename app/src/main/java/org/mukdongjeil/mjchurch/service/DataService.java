package org.mukdongjeil.mjchurch.service;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.models.Board;
import org.mukdongjeil.mjchurch.models.Gallery;
import org.mukdongjeil.mjchurch.models.GalleryDetail;
import org.mukdongjeil.mjchurch.models.ImagePageUrl;
import org.mukdongjeil.mjchurch.models.Sermon;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by gradler on 22/05/2017.
 */

public class DataService {

    public static void insertToRealm(Realm realm, final RealmObject object) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(object);
            }
        });
    }

    public static RealmResults<ImagePageUrl> getImagePageUrls(Realm realm, int pageType) {
        long oneDayGap = System.currentTimeMillis() - Const.DAY_IN_MILLIS;
        return realm.where(ImagePageUrl.class).equalTo("type", pageType)
                .greaterThan("updatedAt", oneDayGap).findAll();
    }

    public static RealmResults<Sermon> getSermonList(Realm realm, int sermonType) {
        return realm.where(Sermon.class).equalTo("sermonType", sermonType).findAll();
    }

    public static Sermon getSermon(Realm realm, String bbsNo) {
        return realm.where(Sermon.class).equalTo("bbsNo", bbsNo).findFirst();
    }

    public static Sermon getSermonByDownloadQueryId(Realm realm, long downloadQueryId) {
        return realm.where(Sermon.class).equalTo("downloadQueryId", downloadQueryId).findFirst();
    }

    public static RealmResults<Board> getBoardList(Realm realm) {
        return realm.where(Board.class).findAll();
    }

    public static RealmResults<Gallery> getGalleryList(Realm realm, int boardType) {
        return realm.where(Gallery.class).equalTo("boardType", boardType).findAll();
    }

    public static Board getBoard(Realm realm, final String contentUrl) {
        return realm.where(Board.class).equalTo("contentUrl", contentUrl).findFirst();
    }

    public static Gallery getGallery(Realm realm, final String photoUrl) {
        return realm.where(Gallery.class).equalTo("photoUrl", photoUrl).findFirst();
    }

    public static GalleryDetail getGalleryDetail(Realm realm, String contentNo) {
        return realm.where(GalleryDetail.class).equalTo("contentNo", contentNo).findFirst();
    }
}
