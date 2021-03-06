package org.mukdongjeil.mjchurch.protocols;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.StringUtils;
import org.mukdongjeil.mjchurch.models.Board;

/**
 * Created by gradler on 2016. 9. 30..
 */
public class RequestBoardContentTask extends RequestBaseTask {
    private static final String TAG = RequestBoardContentTask.class.getSimpleName();

    private OnResultListener listener;
    private String contentUrl;
    private int position;

    public RequestBoardContentTask(int position, String contentUrl, OnResultListener listener) {
        this.listener = listener;
        this.position = position;
        this.contentUrl = contentUrl;
        execute(Const.BASE_URL + contentUrl);
    }

    @Override
    protected void onResult(Source source) {
        if (source != null) {
            Board item = new Board();
            item.contentUrl = this.contentUrl;
            item.title = source.getFirstElementByClass("bbs_ttl").getTextExtractor().toString();
            item.writer = source.getFirstElementByClass("bbs_writer").getTextExtractor().toString();
            item.date = source.getFirstElementByClass("bbs_date").getTextExtractor().toString();
            StringBuilder contentBuffer = new StringBuilder();
            Element contentElement = source.getFirstElementByClass("bbs_substance_p");
            if (contentElement != null) {
                for (Element divElem : contentElement.getAllElements()) {
                    String temp = divElem.getTextExtractor().toString();
                    if (!TextUtils.isEmpty(temp)) {
                        contentBuffer.append(temp);
                    }
                }
            }
            String originalContent = contentBuffer.toString();
            item.content = StringUtils.removeDuplicationSentence(originalContent);

            Logger.d(TAG, "add item : " + item.toString());
            listener.onResult(item, position);
        } else {

            Logger.e(TAG, "source is null");
            listener.onResult(null, OnResultListener.POSITION_NONE);
        }
    }
}