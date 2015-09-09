package org.mukdongjeil.mjchurch.protocol;

import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.util.Logger;

/**
 * Created by Kim SungJoong on 2015-08-24.
 */
public class RequestImageTask extends RequestBaseTask {
    private static final String TAG = RequestImageTask.class.getSimpleName();

    private OnResultListener listener;

    public RequestImageTask(String url, OnResultListener listener) {
        this.listener = listener;
        execute(url);
    }

    @Override
    protected void onResult(Source source) {
        if (listener != null) {
            if (source != null) {
                Element tab1 = source.getElementById("tab1");
                if (tab1 != null) {
                    Element imgElement = tab1.getFirstElement(HTMLElementName.IMG);
                    String src = imgElement.getAttributeValue("src");
                    src = src.replaceAll("&amp;", "&");
                    if (!src.contains("http")) {
                        src = Const.BASE_URL + src;
                    }
                    ImageLoader.getInstance().loadImage(src, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {}
                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                            listener.onResult(failReason, OnResultListener.POSITION_NONE);
                        }
                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            listener.onResult(bitmap, OnResultListener.POSITION_NONE);
                        }
                        @Override
                        public void onLoadingCancelled(String s, View view) {
                            listener.onResult(s, OnResultListener.POSITION_NONE);
                        }
                    });
                } else {
                    Logger.e(TAG, "tab1 element is null");
                    listener.onResult(null, OnResultListener.POSITION_NONE);
                }
            } else {
                Logger.e(TAG, "source is null");
                listener.onResult(null, OnResultListener.POSITION_NONE);
            }
        } else {
            Logger.e(TAG, "cannot send result caused by OnResultListener is null");
        }
    }
}
