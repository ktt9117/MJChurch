package org.mukdongjeil.mjchurch.protocol;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTagType;
import net.htmlparser.jericho.TextExtractor;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.PreferenceUtil;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.database.DBManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Kim on 2015-08-21.
 */
public class RequestSermonsTask extends RequestBaseTask {
    private static final String TAG = RequestSermonsTask.class.getSimpleName();

    private OnResultListener listener;
    private OnResultNoneListener noneListener;
    private int sermonType;
    private int pageNo;

    public RequestSermonsTask(int sermonType, int pageNo, OnResultListener listener) {
        this.listener = listener;
        this.sermonType = sermonType;
        this.pageNo = pageNo;
        execute(Const.getWorshipListUrl(sermonType, pageNo));
    }

    public RequestSermonsTask(int sermonType, int pageNo, OnResultListener listener, OnResultNoneListener noneListener) {
        this.listener = listener;
        this.noneListener = noneListener;
        this.sermonType = sermonType;
        this.pageNo = pageNo;
        execute(Const.getWorshipListUrl(sermonType, pageNo));
    }

    @Override
    protected void onResult(Source source) {
        if (listener != null) {
            //listener.onResult();
            if (source != null) {
                Element contentElement = source.getFirstElementByClass("contents bbs_list");
                if (contentElement != null) {
                    //Logger.i(TAG, "contentElement : " + contentElement.toString());
                    List<SermonItem> serverSermonList = new ArrayList<>();
                    List<Element> linkList = contentElement.getAllElementsByClass("list_link");
                    for (Element link : linkList) {
                        String linkAttr = link.getAttributeValue("href");
                        //Logger.i(TAG, "link : " + linkAttr);
                        if (!TextUtils.isEmpty(linkAttr)) {
                            SermonItem item = new SermonItem();
                            item.contentUrl = linkAttr;
                            String bbsNo = linkAttr.substring(linkAttr.lastIndexOf("=") + 1);
                            //Logger.i(TAG, "bbsNo : " + bbsNo);
                            item.bbsNo = bbsNo;
                            serverSermonList.add(item);
                        }
                    }

                    // compare between local database and server item list.
                    List<SermonItem> localSermonList = DBManager.getInstance(SystemHelpers.getApplicationContext()).getSermonList(sermonType);
                    boolean existLocalItem = false;
                    if (localSermonList != null && localSermonList.size() > 0) {
                        existLocalItem = true;
                    }

                    if (serverSermonList.size() > 0) {
                        for (SermonItem serverItem : serverSermonList) {
                            boolean isExistItem = false;
                            if (existLocalItem) {
                                for (SermonItem localItem : localSermonList) {
                                    if (localItem.bbsNo.equals(serverItem.bbsNo)) {
                                        isExistItem = true;
                                        if (listener != null) {
                                            listener.onResult(localItem, OnResultListener.POSITION_NONE);
                                        }
                                        break;
                                    }
                                }
                            }
                            if (isExistItem == false) {
                                Logger.d(TAG, "The sermon is not exist local db. request new bbs " + serverItem.bbsNo);
                                new RequestSermonTask(serverItem).execute(Const.getWorshipContentUrl(sermonType, pageNo, serverItem.bbsNo));
                            }
                        }
                    } else {
                        //서버에 설교가 없는 경우
                        if (noneListener != null) {
                            noneListener.onResultNone();
                        }
                    }
                } else {
                    Logger.e(TAG, "contentElement is null");
                    Logger.i(TAG, "source : " + source.toString());
                }
            } else {
                Logger.e(TAG, "source is null");
            }

        } else {
            Logger.e(TAG, "cannot send result caused by OnResultListener is null");
        }
    }

    protected class RequestSermonTask extends RequestBaseTask {

        private SermonItem item;

        public RequestSermonTask(SermonItem item) {
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
            if (source != null) {
                Element contentElement = source.getFirstElementByClass("contents bbs_list");
                if (contentElement != null) {
                    Logger.i(TAG, "contentElement : " + contentElement.toString());

                    //extract title & date
                    Element ttlElement = contentElement.getFirstElementByClass("bbs_ttl");
                    if (ttlElement != null) {
                        Logger.i(TAG, "onPostExecute > ttlElement : " + ttlElement.getTextExtractor().toString());
                        String temp = ttlElement.getTextExtractor().toString();
                        if (!TextUtils.isEmpty(temp) && temp.length() > 12) {
                            try {
                                String date = temp.substring(0, 12);
                                item.title = temp.substring(13, temp.length());
                                item.date = date;
                            } catch (Exception e) {
                                e.printStackTrace();
                                item.title = temp;
                            }
                        } else {
                            item.title = temp;
                        }
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
//                            else {
//                                item.chapterInfo += element.getTextExtractor().toString();
//                            }
                        }
                    }

                    //extract content text
                    //TODO : 아래와 같이 하면 설교 본문이 중복되어 표시되어 일단 주석처리함. 추후 수정필요
                    //TextExtractor contentText = temp.getTextExtractor();
                    //item.content = contentText.toString();

                    //extract attached file
                    List<Element> fileElement = contentElement.getAllElementsByClass("attch_file");
                    for (Element elem : fileElement) {
                        //Logger.i(TAG, "file element : " + elem.toString());
                        String href = elem.getFirstElement(HTMLElementName.A).getAttributeValue("href");
                        if (elem.getFirstElement(HTMLElementName.A).getAttributeValue("href").contains(".mp3")) {
                            item.audioUrl = href;
                        } else if (elem.getFirstElement(HTMLElementName.A).getAttributeValue("href").contains("hwp")) {
                            item.docUrl = href;
                        }
                    }
                    Logger.i(TAG, "worshipItem : " + item.toString());

//                  mAdapter.add(item);

                    int insertResult = DBManager.getInstance(SystemHelpers.getApplicationContext()).insertSermon(item, sermonType);
                    item._id = insertResult;
                    Logger.d(TAG, "insert item to local database result : " + insertResult);

                    if (listener != null) {
                        listener.onResult(item, OnResultListener.POSITION_NONE);
                    }

                } else {
                    Logger.e(TAG, "contentElement is null");
                    Logger.i(TAG, "source : " + source.toString());
                }
            } else {
                Logger.e(TAG, "source is null");
            }
        }
    }
}
