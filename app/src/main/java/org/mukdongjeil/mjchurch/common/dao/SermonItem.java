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
    public DownloadStatus downloadStatus;

    public SermonItem() {
        _id = -1;
        downloadQueryId = -1;
        downloadStatus = DownloadStatus.DOWNLOAD_NONE;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public enum DownloadStatus {
        DOWNLOAD_NONE(0),
        DOWNLOAD_START(1),
        DOWNLOAD_SUCCESS(2),
        DOWNLOAD_FAILED(3);

        int value;
        DownloadStatus(int value) {
            this.value = value;
        }

        public static DownloadStatus parse(int value) {
            switch(value) {
                case 1: return DOWNLOAD_START;
                case 2: return DOWNLOAD_SUCCESS;
                case 3: return DOWNLOAD_FAILED;
                case 0:
                default:
                    return DOWNLOAD_NONE;
            }
        }
    }
}
