package org.mukdongjeil.mjchurch.protocol;

import android.os.AsyncTask;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.MainApplication;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Kim on 2015-08-21.
 */
public class RequestImageURLsTask extends AsyncTask<String, Void, List<String>> {

    private static final String TAG = RequestImageURLsTask.class.getSimpleName();

    private RequestBaseTask.OnResultListener listener;

    public RequestImageURLsTask(RequestBaseTask.OnResultListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<String> doInBackground(String... params) {
        if (params == null || params[0] == null) {
            return null;
        }

        List<String> urlList = new ArrayList<>();

        try {
            for (String param : params) {
                URL url = new URL(param);
                Logger.d(TAG, "request url : " + url.toString());
                urlList.add(extractImageUrl(new Source(url)));
            }
        } catch (IOException e) {
            MainApplication.REQUEST_FAIL_COUNT++;
            e.printStackTrace();
        }

        return urlList;
    }

    @Override
    protected void onPostExecute(List<String> results) {
        super.onPostExecute(results);
        if (listener != null) {
            listener.onResult(results, RequestBaseTask.OnResultListener.POSITION_NONE);
        }
    }

    private String extractImageUrl(Source source) {
        if (source != null) {
            Element tab1 = source.getElementById("tab1");

            if (tab1 != null) {
                Element imgElement = tab1.getFirstElement(HTMLElementName.IMG);
                String src = imgElement.getAttributeValue("src");
                src = src.replaceAll("&amp;", "&");
                if (!src.contains("http")) {
                    src = Const.BASE_URL + src;
                }

                return src;

            } else {
                Logger.e(TAG, "tab1 element is null");
            }
        } else {
            Logger.e(TAG, "source is null");
        }

        return "";
    }
}
