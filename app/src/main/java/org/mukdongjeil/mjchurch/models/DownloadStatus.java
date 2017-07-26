package org.mukdongjeil.mjchurch.models;

/**
 * Created by gradler on 15/05/2017.
 */

public enum DownloadStatus {
    NONE(0),
    START(1),
    COMPLETE(2),
    FAILED(3);

    int value;
    DownloadStatus(int value) {
        this.value = value;
    }

    public static DownloadStatus parse(int value) {
        switch(value) {
            case 1: return START;
            case 2: return COMPLETE;
            case 3: return FAILED;
            case 0:
            default:
                return NONE;
        }
    }
}
