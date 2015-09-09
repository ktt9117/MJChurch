package org.mukdongjeil.mjchurch.protocol;

import android.text.TextUtils;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.board.BoardFragment;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.GalleryItem;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John Kim on 2015-08-21.
 */
public class RequestListTask extends RequestBaseTask {
    private static final String TAG = RequestListTask.class.getSimpleName();

    private OnResultListener listener;
    private int boardType;

    public RequestListTask(int boardType, int pageNo, OnResultListener listener) {
        this.listener = listener;
        this.boardType = boardType;
        String requestUrl;
        switch (boardType) {
            case BoardFragment.BOARD_TYPE_GALLERY :
                requestUrl = Const.getGalleryListUrl(pageNo);
                break;
            case BoardFragment.BOARD_TYPE_NEW_PERSON :
                requestUrl = Const.getNewPersonListUrl(pageNo);
                break;
            case BoardFragment.BOARD_TYPE_THANKS_SHARING :
            default :
                requestUrl = Const.getThanksShareListUrl(pageNo);
                break;
        }
        execute(requestUrl);
    }

    @Override
    protected void onResult(Source source) {
        if (listener != null) {
            if (source != null) {
                if (boardType == BoardFragment.BOARD_TYPE_THANKS_SHARING) {
                    Element contentElement = source.getFirstElementByClass("contents bbs_list");
                    if (contentElement != null) {
                        parseBoardList(contentElement);

                    } else {
                        Logger.e(TAG, "contentElement is null");
                        Logger.i(TAG, "source : " + source.toString());
                        listener.onResult(null, OnResultListener.POSITION_NONE);
                    }
                } else /*if (boardType == BoardFragment.BOARD_TYPE_GALLERY)*/ {
                    Element contentElement = source.getFirstElementByClass("contents photo_list");
                    if (contentElement != null) {
                        parseGalleryList(contentElement);

                    } else {
                        Logger.e(TAG, "contentElement is null");
                        Logger.i(TAG, "source : " + source.toString());
                        listener.onResult(null, OnResultListener.POSITION_NONE);
                    }
                }
            } else {
                Logger.e(TAG, "source is null");
                listener.onResult(null, OnResultListener.POSITION_NONE);
            }
        } else {
            Logger.e(TAG, "cannot send result caused by OnResultListener is null");
        }
    }

    private void parseBoardList(Element contentElement) {
        Logger.i(TAG, "contentElement : " + contentElement.toString());
//        List<BoardItem> itemList = new ArrayList<>();
        List<Element> linkList = contentElement.getAllElementsByClass("list_link");
//        List<Element> titleList = contentElement.getAllElementsByClass("bbs_ttl");
//        List<Element> writerList = contentElement.getAllElementsByClass("bbs_writer");
//        List<Element> dateList = contentElement.getAllElementsByClass("bbs_date");

        int loopCount = linkList.size();
        if (linkList != null && loopCount > 0) {
//            for (int i = 0; i < linkList.size(); i++) {
//                BoardItem item = new BoardItem();
//
//                String linkAttr = linkList.get(i).getAttributeValue("href");
//                //Logger.i(TAG, "link : " + linkAttr);
//                if (!TextUtils.isEmpty(linkAttr)) {
//                    item.contentUrl = linkAttr;
//                    String bbsNo = linkAttr.substring(linkAttr.lastIndexOf("=") + 1);
//                    item.bbsNo = bbsNo;
//                }
//                try {
//                    item.title = titleList.get(i).getTextExtractor().toString();
//                    item.writer = writerList.get(i).getTextExtractor().toString();
//                    item.date = dateList.get(i).getTextExtractor().toString();
//                } catch (ArrayIndexOutOfBoundsException aiobe) {
//                    aiobe.printStackTrace();
//                    item = null;
//                } catch (NullPointerException npe) {
//                    npe.printStackTrace();
//                    item = null;
//                }
//
//                if (item != null) {
//                    Logger.d(TAG, "add item : " + item.toString());
//                    itemList.add(item);
//                }
//            }
//            listener.onResult(itemList);
            listener.onResult(linkList, OnResultListener.POSITION_NONE);

        } else {
            listener.onResult(null, OnResultListener.POSITION_NONE);
            Logger.e(TAG, "there is not linkList child item");
        }
    }

    private void parseGalleryList(Element contentElement) {
        Logger.i(TAG, "contentElement : " + contentElement.toString());
        List<GalleryItem> itemList = new ArrayList<>();
        List<Element> linkList = contentElement.getAllElementsByClass("photo_list_a");
        List<Element> photoUrlList = contentElement.getAllElementsByClass("photo_list_img");
        List<Element> titleList = contentElement.getAllElementsByClass("photo_list_ttl");
        List<Element> dateList = contentElement.getAllElementsByClass("photo_list_date");

        int loopCount = linkList.size();
        if (linkList != null && loopCount > 0) {
            for (int i = 0; i < loopCount; i++) {
                GalleryItem item = new GalleryItem();

                String linkAttr = linkList.get(i).getAttributeValue("href");
                if (!TextUtils.isEmpty(linkAttr)) {
                    item.contentUrl = linkAttr;
                    String bbsNo = linkAttr.substring(linkAttr.lastIndexOf("=") + 1);
                    item.bbsNo = bbsNo;
                }

                try {
                    String src = photoUrlList.get(i).getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                    if (!TextUtils.isEmpty(src)) {
                        src = src.replaceAll("&amp;", "&");
                        item.photoUrl = src;
                        Logger.d(TAG, "parseGalleryList photoUrl : " + src);
                    }
                    item.title = titleList.get(i).getTextExtractor().toString();
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
            listener.onResult(itemList, OnResultListener.POSITION_NONE);

        } else {
            listener.onResult(null, OnResultListener.POSITION_NONE);
            Logger.e(TAG, "there is not linkList child item");
        }
    }
}