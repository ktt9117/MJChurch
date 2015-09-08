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

import java.util.List;

/**
 * Created by Kim SungJoong on 2015-08-24.
 */
public class RequestImageListTask extends RequestBaseTask {
    private static final String TAG = RequestImageListTask.class.getSimpleName();

    private OnResultListener listener;

    public RequestImageListTask(String url, OnResultListener listener) {
        this.listener = listener;
        execute(url);
    }

    @Override
    protected void onResult(Source source) {
        if (listener != null) {
            if (source != null) {
                Element imgBoxElement = source.getFirstElementByClass("img_box");
                Logger.e(TAG, "imgBoxElement : " + imgBoxElement.toString());
                if (imgBoxElement != null) {
                    List<Element> imgTagList = imgBoxElement.getAllElements(HTMLElementName.IMG);
                    Logger.e(TAG, "imgTagList count : " + ((imgTagList != null) ? imgTagList.size() : -1));
                    listener.onResult(imgTagList);
                }
            } else {
                Logger.e(TAG, "source is null");
                listener.onResult(null);
            }
        } else {
            Logger.e(TAG, "cannot send result caused by OnResultListener is null");
        }
    }
}
