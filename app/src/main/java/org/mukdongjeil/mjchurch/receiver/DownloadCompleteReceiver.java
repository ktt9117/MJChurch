package org.mukdongjeil.mjchurch.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.database.DBManager;
import org.mukdongjeil.mjchurch.sermon.SermonFragment;

/**
 * Created by Kim SungJoong on 2015-09-10.
 */
public class DownloadCompleteReceiver extends BroadcastReceiver{
    private static final String TAG = DownloadCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "onReceive() action : " + intent.getAction() != null ? intent.getAction() : "empty...");

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            Toast.makeText(context, "다운로드 완료", Toast.LENGTH_LONG).show();
            //로컬 DB에 있는 설교 목록에서 downloadId에 해당하는 항목을 찾아 downloadStatus값을 갱신한다.
            if (intent != null && intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                int res = DBManager.getInstance(context).updateSermonDownloadStatus(downloadId, SermonItem.DownloadStatus.DOWNLOAD_SUCCESS);
                Logger.i(TAG, "update download status result : " + res);
            } else {
                Logger.e(TAG, "download done but do nothing! caused by there is no EXTRA_DOWNLOAD_ID");
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SermonFragment.INTENT_ACTION_DOWNLOAD_COMPLETED));
        } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            // TODO : 추후 다운로드 아이템이 속해 있는 설교 목록 리스트를 보여준다.
            Logger.i(TAG, "download item notification clicked. do nothing");
        }
        /*
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_PENDING|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PAUSED);
        Logger.i(TAG, "=========== Download Pending|Running|Paused Item Query ==========");
        Cursor c = downloadManager.query(query);
        if (c != null && c.getCount() > 0) {
            while(c.moveToNext()) {
                Logger.i(TAG, "COLUMN_LAST_MODIFIED_TIMESTAMP  : " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
                Logger.i(TAG, "COLUMN_DESCRIPTION : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)));
                Logger.i(TAG, "COLUMN_LOCAL_FILENAME  : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)));
                Logger.i(TAG, "COLUMN_LOCAL_URI   : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                Logger.i(TAG, "COLUMN_MEDIAPROVIDER_URI  : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI)));
                Logger.i(TAG, "COLUMN_MEDIA_TYPE : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
                Logger.i(TAG, "COLUMN_URI    : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)));
                Logger.i(TAG, "COLUMN_REASON  : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                Logger.i(TAG, "COLUMN_STATUS (16:fail, 4:pause, 1:pending, 2:running, 8:successful)  : " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
                Logger.i(TAG, "COLUMN_TITLE   : " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)));
                Logger.i(TAG, "COLUMN_TOTAL_SIZE_BYTES   : " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
                Logger.i(TAG, "COLUMN_BYTES_DOWNLOADED_SO_FAR : " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
                Logger.i(TAG, "===============================================");

//                [MJChurch] COLUMN_LAST_MODIFIED_TIMESTAMP  : -1237093356
//                [MJChurch] COLUMN_DESCRIPTION : 하나님이 찾으시는 사람 "돕는 배필(2)"
//                [MJChurch] COLUMN_LOCAL_FILENAME  : /storage/emulated/0/Download/하나님이 찾으시는 사람 "돕는 배필(2)".mp3
//                [MJChurch] COLUMN_LOCAL_URI   : file:///storage/emulated/0/Download/%ED%95%98%EB%82%98%EB%8B%98%EC%9D%B4%20%EC%B0%BE%EC%9C%BC%EC%8B%9C%EB%8A%94%20%EC%82%AC%EB%9E%8C%20%22%EB%8F%95%EB%8A%94%20%EB%B0%B0%ED%95%84(2)%22.mp3
//                [MJChurch] COLUMN_MEDIAPROVIDER_URI  : null
//                [MJChurch] COLUMN_MEDIA_TYPE : audio/mp3
//                [MJChurch] COLUMN_URI    : http://mukdongjeil.hompee.org/servlet/SambaDown?path=file/60225/music/143441847596.mp3&realName=20150614_111618 주일 2부 - 돕는 배필(2).mp3
//                [MJChurch] COLUMN_REASON  : placeholder
//                [MJChurch] COLUMN_STATUS (16:fail, 4:pause, 1:pending, 2:running, 8:successful)  : 2
//                [MJChurch] COLUMN_TITLE   : 설교 다운로드 중...
//                [MJChurch] COLUMN_TOTAL_SIZE_BYTES   : 37706484
//                [MJChurch] COLUMN_BYTES_DOWNLOADED_SO_FAR : 27000832
            }
            c.close();
        } else {
            Logger.i(TAG, "cursor is null or count <= 0");
        }
        */
    }
}
