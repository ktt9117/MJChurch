package org.mukdongjeil.mjchurch.protocol;

import android.content.Context;
import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.BoardItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.StringUtils;
import org.mukdongjeil.mjchurch.database.DBManager;

/**
 * Created by gradler on 2016. 9. 30..
 */
public class RequestBoardContentTask extends RequestBaseTask {
    private static final String TAG = RequestBoardContentTask.class.getSimpleName();

    private Context context;
    private OnResultListener listener;
    private String contentUrl;
    private int position;

    public RequestBoardContentTask(Context context, int position, String contentUrl, OnResultListener listener) {
        this.context = context;
        this.listener = listener;
        this.position = position;
        this.contentUrl = contentUrl;
        execute(Const.BASE_URL + contentUrl);
    }

    @Override
    protected void onResult(Source source) {
        if (source != null) {
            BoardItem item = new BoardItem();
            item.contentUrl = this.contentUrl;
            item.title = source.getFirstElementByClass("bbs_ttl").getTextExtractor().toString();
            item.writer = source.getFirstElementByClass("bbs_writer").getTextExtractor().toString();
            item.date = source.getFirstElementByClass("bbs_date").getTextExtractor().toString();
            StringBuffer contentBuffer = new StringBuffer();
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
            Logger.e(TAG, "check check2");

            if (item != null) {
                Logger.d(TAG, "add item : " + item.toString());
                int res = DBManager.getInstance(context).insertThankShare(item);
                Logger.i(TAG, "insert to local database result : " + res);
            }

            listener.onResult(item, position);

        } else {
            Logger.e(TAG, "source is null");
            listener.onResult(null, OnResultListener.POSITION_NONE);
        }
    }
}