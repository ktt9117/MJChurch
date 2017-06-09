package org.mukdongjeil.mjchurch.common.util;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.models.DownloadStatus;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.service.DataService;

import java.io.File;

import io.realm.Realm;

/**
 * Created by Kim SungJoong on 2015-09-14.
 */
public class DownloadUtil {
    private static final String TAG = DownloadUtil.class.getSimpleName();

    public static boolean isNecessaryDownload(Context context, Sermon item) {
        // 1. 다운로드 폴더에 (#title).mp3와 동일한 이름의 파일이 있는지 체크
//        if (isExistsDownloadFile(item)) {
            // 1-1. 파일이 있는 경우 : 다운로드 실패 목록중에 해당 항목이 존재하는지 체크
            return !isDownloadFailedItem(context, item);
//        } else {
            // 1-2. 파일이 없는 경우 : 다운로드 요청
//            return true;
//        }
    }

    public static boolean isDownloadSuccessItem(Context context, Sermon item) {
//        boolean isExistsDownloadFile = isExistsDownloadFile(item);
//        Logger.d(TAG, "isExistsDownloadFile : " + isExistsDownloadFile);
//        if (isExistsDownloadFile) {
        if (DownloadStatus.parse(item.downloadStatus) == DownloadStatus.DOWNLOAD_SUCCESS) {
            boolean isDownloadFailedItem = isDownloadFailedItem(context, item);
            Logger.d(TAG, "isDownloadFailedItem : " + isDownloadFailedItem);
            return !isDownloadFailedItem;
        } else {
            return false;
        }
    }

    public static void requestDownload(Context context, final Sermon item) {
        Logger.e(TAG, "try to download");
        Uri downloadUri = Uri.parse(Const.BASE_URL + item.audioUrl);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setTitle("설교 다운로드");
        request.setDescription(item.titleWithDate);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.titleWithDate + StringUtils.FILE_EXTENSION_MP3);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//            request.setShowRunningNotification(true);
//        } else {
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadQueryId = downloadManager.enqueue(request);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sermon queryItem = DataService.getSermon(realm, item.bbsNo);
                if (queryItem != null) {
                    queryItem.downloadStatus = DownloadStatus.DOWNLOAD_START.ordinal();
                    queryItem.downloadQueryId = downloadQueryId;
                    Logger.i(TAG, "update current item's download status to DOWNLOAD_START");
                } else {
                    Logger.e(TAG, "cannot update current item's download status caused by query result is not exists");
                }

                realm.close();
            }
        });
    }

    private static boolean isExistsDownloadFile(Sermon item) {
        // 1. 다운로드 폴더가 없을 경우를 대비해 폴더 생성
        Const.DIR_PUB_DOWNLOAD.mkdirs();

        // 2. 다운로드 폴더에 (#title).mp3와 동일한 이름의 파일이 있는지 체크
        File file = new File(Const.DIR_PUB_DOWNLOAD, item.titleWithDate + StringUtils.FILE_EXTENSION_MP3);
        Logger.e(TAG, "check file : " + file.getAbsolutePath());
        return file.exists();
    }

    private static boolean isDownloadFailedItem(Context context, Sermon item) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_FAILED);
        Logger.i(TAG, "=========== Download Failed Item Query ==========");
        Cursor c = downloadManager.query(query);
        boolean downloadFailedFileExists = false;
        if (c != null && c.getCount() > 0) {
            boolean loopFlag = true;
            while (c.moveToNext() && loopFlag) {
                String columnUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                Logger.i(TAG, "COLUMN_URI : " + columnUri);
                Logger.i(TAG, "item.audioUrl : " + Const.BASE_URL + item.audioUrl);
                if (columnUri.equals(Const.BASE_URL + item.audioUrl)) {
                    //기존에 있던 파일이 정상적으로 다운로드 되지 않은 파일이므로 삭제
                    File file = new File(Const.DIR_PUB_DOWNLOAD, item.titleWithDate + StringUtils.FILE_EXTENSION_MP3);
                    file.delete();
                    loopFlag = false;
                    downloadFailedFileExists = true;
                }
            }
            c.close();
            return downloadFailedFileExists;
        } else {
            Logger.i(TAG, "cursor is null or count <= 0");
            return false;
        }

    }
}
