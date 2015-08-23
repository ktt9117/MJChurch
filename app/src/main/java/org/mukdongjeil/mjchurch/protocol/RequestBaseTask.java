package org.mukdongjeil.mjchurch.protocol;

import android.os.AsyncTask;

import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.util.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Created by John Kim on 2015-08-21.
 */
public abstract class RequestBaseTask extends AsyncTask<String, Void, Source> {
    private static final String TAG = RequestBaseTask.class.getSimpleName();

    public interface OnResultListener {
        void onResult(Object obj);
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
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Source segments) {
        super.onPostExecute(segments);
        Logger.d(TAG, "onPostExecute : " + segments);
        onResult(segments);
    }

    protected abstract void onResult(Source source);
}
