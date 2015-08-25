package org.mukdongjeil.mjchurch.protocol;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.BoardItem;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.database.DBManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Kim on 2015-08-21.
 */
public class RequestListTask extends RequestBaseTask {
    private static final String TAG = RequestListTask.class.getSimpleName();

    private OnResultListener listener;
    private int pageNo;

    public RequestListTask(int pageNo, OnResultListener listener) {
        this.listener = listener;
        this.pageNo = pageNo;
        execute(Const.BOARD_THANKS_SHARE_URL);
    }

    @Override
    protected void onResult(Source source) {
        if (listener != null) {
            if (source != null) {
                Element contentElement = source.getFirstElementByClass("contents bbs_list");
                if (contentElement != null) {
                    Logger.i(TAG, "contentElement : " + contentElement.toString());
                    List<BoardItem> itemList = new ArrayList<>();
                    List<Element> linkList = contentElement.getAllElementsByClass("list_link");
                    List<Element> titleList = contentElement.getAllElementsByClass("bbs_ttl");
                    List<Element> writerList = contentElement.getAllElementsByClass("bbs_writer");
                    List<Element> dateList = contentElement.getAllElementsByClass("bbs_date");
                    for (int i = 0; i < linkList.size(); i++) {
                        BoardItem item = new BoardItem();

                        String linkAttr = linkList.get(i).getAttributeValue("href");
                        //Logger.i(TAG, "link : " + linkAttr);
                        if (!TextUtils.isEmpty(linkAttr)) {
                            item.contentUrl = linkAttr;
                            String bbsNo = linkAttr.substring(linkAttr.lastIndexOf("=") + 1);
                            item.bbsNo = bbsNo;
                        }
                        try {
                            item.title = titleList.get(i).getTextExtractor().toString();
                            item.writer = writerList.get(i).getTextExtractor().toString();
                            item.date = dateList.get(i).getTextExtractor().toString();
                        } catch (ArrayIndexOutOfBoundsException aiobe) {
                            aiobe.printStackTrace();
                            item = null;
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                            item = null;
                        }

                        if (item != null) {
                            Logger.d(TAG, "add item : " + item.toString());
                            itemList.add(item);
                        }
                    }

                    listener.onResult(itemList);

                } else {
                    Logger.e(TAG, "contentElement is null");
                    Logger.i(TAG, "source : " + source.toString());
                    listener.onResult(null);
                }
            } else {
                Logger.e(TAG, "source is null");
                listener.onResult(null);
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
                        }
                    }

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

                    //int insertResult = DBManager.getInstance(SystemHelpers.getApplicationContext()).insertData(item, sermonType);
                    //Logger.d(TAG, "insert item to local database result : " + insertResult);

                    if (listener != null) {
                        listener.onResult(item);
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
