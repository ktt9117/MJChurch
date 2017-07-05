package org.mukdongjeil.mjchurch.protocol;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.models.Sermon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by John Kim on 2015-08-21.
 */
public class RequestSermonListTask extends RequestBaseTask {
    private static final String TAG = RequestSermonListTask.class.getSimpleName();

    public interface OnSermonResultListener {
        void onResult(boolean hasItem, Sermon item);
    }

    private OnSermonResultListener onSermonResultListener;
    private int sermonType;

    private RealmResults<Sermon> localItemList;

    public RequestSermonListTask(int sermonType, RealmResults<Sermon> localItemList, OnSermonResultListener listener) {
        this.onSermonResultListener = listener;
        this.sermonType = sermonType;
        this.localItemList = localItemList;
        execute(Const.getWorshipListUrl(sermonType, 1));
    }

    @Override
    protected void onResult(Source source) {
        if (onSermonResultListener == null) {
            Logger.e(TAG, "cannot send result caused by OnResultListener is null");
            return;
        }

        if (source == null) {
            Logger.e(TAG, "source is null");
            onSermonResultListener.onResult(false, null);
            return;
        }

        Element contentElement = source.getFirstElementByClass("contents bbs_list");
        //Logger.i(TAG, "contentElement : " + contentElement.toString());
        if (contentElement == null) {
            Logger.e(TAG, "contentElement is null");
            Logger.i(TAG, "source : " + source.toString());
            onSermonResultListener.onResult(false, null);
            return;
        }

        List<Sermon> serverSermonList = new ArrayList<>();
        List<Element> linkList = contentElement.getAllElementsByClass("list_link");
        for (Element link : linkList) {
            String linkAttr = link.getAttributeValue("href");
            //Logger.i(TAG, "link : " + linkAttr);
            if (!TextUtils.isEmpty(linkAttr)) {
                Sermon item = new Sermon();
                item.sermonType = sermonType;
                item.contentUrl = linkAttr;
                item.bbsNo = linkAttr.substring(linkAttr.lastIndexOf("=") + 1);
                serverSermonList.add(item);
            }
        }

        // compare between local database and server item list.
        if (serverSermonList.size() > 0) {
            for (Sermon serverItem : serverSermonList) {
                boolean isExistItem = false;
                if (localItemList.size() > 0) {
                    for (Sermon localItem : localItemList) {
                        if (localItem.bbsNo.equals(serverItem.bbsNo)) {
                            isExistItem = true;
                            onSermonResultListener.onResult(true, localItem);
                            break;
                        }
                    }
                }

                if (!isExistItem) {
                    Logger.d(TAG, "The sermon is not exist local db. request new bbs " + serverItem.bbsNo);
                    new RequestSermonListTask.RequestSermonTask(serverItem).execute(Const.getWorshipContentUrl(sermonType, 1, serverItem.bbsNo));
                }
            }
        } else {
            //서버에 설교가 없는 경우
            onSermonResultListener.onResult(false, null);
        }
    }

    private class RequestSermonTask extends RequestBaseTask {

        private Sermon item;

        public RequestSermonTask(Sermon item) {
            this.item = item;
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
        protected void onResult(Source source) {
            if (source == null) {
                Logger.e(TAG, "source is null");
                return;
            }

            Element contentElement = source.getFirstElementByClass("contents bbs_list");
            if (contentElement == null) {
                Logger.e(TAG, "contentElement is null");
                Logger.i(TAG, "source : " + source.toString());
                return;
            }

            //extract title & date
            Element ttlElement = contentElement.getFirstElementByClass("bbs_ttl");
            if (ttlElement != null) {
                Logger.i(TAG, "onPostExecute > ttlElement : " + ttlElement.getTextExtractor().toString());
                item.titleWithDate = ttlElement.getTextExtractor().toString();
            } else {
                Logger.e(TAG, "onPostExecute > cannot find ttlElement element");
            }

            //extract preacher and chapterInfo
            Element temp = contentElement.getFirstElementByClass("bbs_substance_p");

            for (Element element : temp.getAllElements(HTMLElementName.STRONG)) {
                String tempStr = element.toString().replaceAll("&nbsp;", " ");
                //Logger.d(TAG, "strong element : " + );
                if (tempStr.contains("<br />")) {
                    String extractStr = element.getTextExtractor().toString();
                    String[] tempArr = null;
                    boolean isTitleSplit = false;
                    if (extractStr.contains("주제 : ")) {
                        tempArr = extractStr.split("주제 : ");
                        isTitleSplit = true;
                    } else if (extractStr.contains("본문 : ")) {
                        tempArr = extractStr.split("본문 : ");
                    }

                    if (tempArr != null && tempArr.length > 0) {
                        item.preacher = tempArr[0];
                        item.chapterInfo = (isTitleSplit ? "주제 : " : "본문 : ") + tempArr[1];
                    } else {
                        item.preacher = element.getTextExtractor().toString();
                    }//
                } else {
                    if (tempStr.contains("설교 : ") || tempStr.contains("강의 : ")) {
                        item.preacher = element.getTextExtractor().toString();
                    } else if (tempStr.contains("본문 : ") || tempStr.contains("주제 : ")) {
                        item.chapterInfo = element.getTextExtractor().toString();
                    }
                }
            }

            //extract content text
            //TODO : 아래와 같이 하면 설교 본문이 중복되어 표시되어 일단 주석처리함. 추후 수정필요
            //TextExtractor contentText = temp.getTextExtractor();
            //item.content = contentText.toString();

            //extract attached file
            List<Element> fileElement = contentElement.getAllElementsByClass("attch_file");
            for (Element elem : fileElement) {
                String href = elem.getFirstElement(HTMLElementName.A).getAttributeValue("href");
                if (elem.getFirstElement(HTMLElementName.A).getAttributeValue("href").contains(".mp3")) {
                    item.audioUrl = href;
                } else if (elem.getFirstElement(HTMLElementName.A).getAttributeValue("href").contains("hwp")) {
                    item.docUrl = href;
                }
            }
            Logger.i(TAG, "worshipItem : " + item.toString());

            onSermonResultListener.onResult(true, item);
        }
    }
}
