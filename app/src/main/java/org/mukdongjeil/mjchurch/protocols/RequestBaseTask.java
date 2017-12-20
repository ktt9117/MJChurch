package org.mukdongjeil.mjchurch.protocols;

import android.os.AsyncTask;

import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.MainApplication;
import org.mukdongjeil.mjchurch.utils.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Created by John Kim on 2015-08-21.
 */
public abstract class RequestBaseTask extends AsyncTask<String, Void, Source> {
    private static final String TAG = RequestBaseTask.class.getSimpleName();

    public interface OnResultListener {
        int POSITION_NONE = -1;
        void onResult(Object obj, int position);
    }

    @Override
    protected Source doInBackground(String... params) {
        if (params == null || params[0] == null) {
            return null;
        }

        try {
            URL url = new URL(params[0]);
            Logger.d(TAG, "request url : " + url.toString());
            return new Source(url);
        } catch (IOException e) {
            MainApplication.REQUEST_FAIL_COUNT++;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Source segments) {
        super.onPostExecute(segments);
        //Logger.d(TAG, "onPostExecute : " + segments);
        onResult(segments);
        if (segments == null) {
            MainApplication.REQUEST_FAIL_COUNT++;
        }

        if (MainApplication.REQUEST_FAIL_COUNT > 2) {
            // TODO: 앱 실행 후 네트워크 요청이 3번 이상 실패 처리되는 경우, 경고 토스트 발생
            //MainApplication.serverDownProcess(context);
        }
    }

    protected abstract void onResult(Source source);
}
