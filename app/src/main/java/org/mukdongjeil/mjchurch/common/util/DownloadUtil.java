package org.mukdongjeil.mjchurch.common.util;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.database.DBManager;

import java.io.File;

/**
 * Created by Kim SungJoong on 2015-09-14.
 */
public class DownloadUtil {
    private static final String TAG = DownloadUtil.class.getSimpleName();

    public static boolean isNecessaryDownload(Context context, SermonItem item) {
        // 1. 다운로드 폴더에 (#title).mp3와 동일한 이름의 파일이 있는지 체크
        if (isExistsDownloadFile(item)) {
            // 1-1. 파일이 있는 경우 : 다운로드 실패 목록중에 해당 항목이 존재하는지 체크
            return isDownloadFailedItem(context, item);
        } else {
            // 1-2. 파일이 없는 경우 : 다운로드 요청
            return true;
        }
    }

    public static boolean isDownloadSuccessItem(Context context, SermonItem item) {
        if (isExistsDownloadFile(item)) {
            return isDownloadFailedItem(context, item) == true ? false : true;
        } else {
            return false;
        }
    }

    public static SermonItem.DownloadStatus getDownloadStatus(Context context, SermonItem item) {
        if (isExistsDownloadFile(item)) {
            return isDownloadFailedItem(context, item) == true ? SermonItem.DownloadStatus.DOWNLOAD_FAILED : SermonItem.DownloadStatus.DOWNLOAD_SUCCESS;
        } else {
            return SermonItem.DownloadStatus.DOWNLOAD_NONE;
        }
    }

    public static long requestDownload(Context context, SermonItem item) {
        Uri downloadUri = Uri.parse(Const.BASE_URL + item.audioUrl);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setTitle("설교 다운로드");
        request.setDescription(item.title);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, item.title + StringUtils.FILE_EXTENSION_MP3);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//            request.setShowRunningNotification(true);
//        } else {
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        }

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadQueryId = downloadManager.enqueue(request);
        int res = DBManager.getInstance(context).updateSermonDownloadStatus(item._id, downloadQueryId, SermonItem.DownloadStatus.DOWNLOAD_START);
        Logger.i(TAG, "update current item's download status result : " + res);
        return downloadQueryId;
    }

    private static boolean isExistsDownloadFile(SermonItem item) {
        // 1. 다운로드 폴더가 없을 경우를 대비해 폴더 생성
        Const.DIR_PUB_DOWNLOAD.mkdirs();

        // 2. 다운로드 폴더에 (#title).mp3와 동일한 이름의 파일이 있는지 체크
        File file = new File(Const.DIR_PUB_DOWNLOAD, item.title + StringUtils.FILE_EXTENSION_MP3);
        return (file != null && file.exists());
    }

    private static boolean isDownloadFailedItem(Context context, SermonItem item) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_FAILED);
        Logger.i(TAG, "=========== Download Failed Item Query ==========");
        Cursor c = downloadManager.query(query);
        if (c != null && c.getCount() > 0) {
            boolean loopFlag = true;
            while (c.moveToNext() && loopFlag) {
                String columnUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                Logger.i(TAG, "COLUMN_URI : " + columnUri);
                Logger.i(TAG, "item.audioUrl : " + Const.BASE_URL + item.audioUrl);
                if (columnUri.equals(Const.BASE_URL + item.audioUrl)) {
                    //기존에 있던 파일이 정상적으로 다운로드 되지 않은 파일이므로 삭제
                    File file = new File(Const.DIR_PUB_DOWNLOAD, item.title + StringUtils.FILE_EXTENSION_MP3);
                    file.delete();
                    return true;
                }
            }
            c.close();
        } else {
            Logger.i(TAG, "cursor is null or count <= 0");
            return false;
        }

        return false;
    }
}
