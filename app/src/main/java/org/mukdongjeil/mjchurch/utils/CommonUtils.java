package org.mukdongjeil.mjchurch.utils;

import org.mukdongjeil.mjchurch.Const;

/**
 * Created by ktt91 on 2017-11-21.
 */

public class CommonUtils {

    public static String getYoutubeThumbnailUrl(String youtubeUrl) {
        String[] videoPath = youtubeUrl.split("/");
        StringBuilder photoUrl = new StringBuilder();
        photoUrl.append(Const.YOUTUBE_THUMB_URL_PREFIX);
        photoUrl.append(videoPath[videoPath.length-1]);
        photoUrl.append(Const.YOUTUBE_THUMB_URL_POSTFIX);
        return photoUrl.toString();
    }
}
