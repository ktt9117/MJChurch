package org.mukdongjeil.mjchurch.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.fragments.SermonFragment;
import org.mukdongjeil.mjchurch.models.DownloadStatus;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.service.DataService;

import io.realm.Realm;

/**
 * Created by Kim SungJoong on 2015-09-10.
 */
public class DownloadCompleteReceiver extends BroadcastReceiver{
    private static final String TAG = DownloadCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.i(TAG, "onReceive() action : " + intent.getAction());

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            Toast.makeText(context, "다운로드 완료", Toast.LENGTH_LONG).show();
            //로컬 DB에 있는 설교 목록에서 downloadId에 해당하는 항목을 찾아 downloadStatus값을 갱신한다.
            if (intent.hasExtra(DownloadManager.EXTRA_DOWNLOAD_ID)) {
                final long downloadQueryId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        Sermon item = DataService.getSermonByDownloadQueryId(realm, downloadQueryId);
                        if (item != null) {
                            item.downloadStatus = DownloadStatus.DOWNLOAD_SUCCESS.ordinal();
                        }
                        realm.close();
                    }
                });

            } else {
                Logger.e(TAG, "download done but do nothing! caused by there is no EXTRA_DOWNLOAD_ID");
            }

            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SermonFragment.INTENT_ACTION_DOWNLOAD_COMPLETED));

        } else if (action.equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            // TODO : 추후 다운로드 아이템이 속해 있는 설교 목록 리스트를 보여준다.
            Logger.i(TAG, "download item notification clicked. do nothing");
        }
    }
}
