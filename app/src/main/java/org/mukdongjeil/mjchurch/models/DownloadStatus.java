package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 15/05/2017.
 */

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
