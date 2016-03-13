package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.BoardItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.database.DBManager;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestListTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-08-25.
 */
public class BoardFragment extends ListFragment {
    private static final String TAG = BoardFragment.class.getSimpleName();

    public static final int BOARD_TYPE_THANKS_SHARING = 17;
    public static final int BOARD_TYPE_GALLERY = 18;
    public static final int BOARD_TYPE_NEW_PERSON = 19;

    private int mPageNo;
    private List<BoardItem> mItemList;
    private List<BoardItem> mLocalItemList;
    private BoardListAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPageNo = 1;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        mLocalItemList = DBManager.getInstance(SystemHelpers.getApplicationContext()).getThankShareList();
        mItemList = new ArrayList<>();
        mAdapter = new BoardListAdapter(getActivity(), mItemList);
        setListAdapter(mAdapter);

        new RequestListTask(BOARD_TYPE_THANKS_SHARING, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                setListShown(true);

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }

                if (obj != null && obj instanceof List) {
                    List<Element> linkList = (List) obj;
                    for (int i = 0; i < linkList.size(); i++) {
                        Element link = linkList.get(i);
                        String href = link.getAttributeValue("href");
                        // compare between local database and server item list.
                        BoardItem localItem = getItemFromLocalDb(href);
                        if (localItem != null) {
                            mAdapter.add(localItem);
                        } else {
                            new RequestBoardContentTask(i, href, new RequestBaseTask.OnResultListener() {
                                @Override
                                public void onResult(Object obj, int position) {
                                    if (obj != null && obj instanceof BoardItem) {
                                        if (position == POSITION_NONE) {
                                            position = 0;
                                        }
                                        mAdapter.add(position, (BoardItem) obj);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private BoardItem getItemFromLocalDb(String href) {
        if (mLocalItemList != null && mLocalItemList.size() > 0) {
            for (BoardItem item : mLocalItemList) {
                if (item.contentUrl.equals(href)) {
                    return item;
                }
            }
        }
        return null;
    }

    private class BoardListAdapter extends ArrayAdapter<BoardItem> {

        private List<BoardItem> itemList;

        public BoardListAdapter(Context context, List<BoardItem> objects) {
            super(context, 0, objects);
            itemList = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_list_board, null);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView writer = (TextView) convertView.findViewById(R.id.writer);
            TextView date = (TextView) convertView.findViewById(R.id.date);
            TextView content = (TextView) convertView.findViewById(R.id.content);

            BoardItem item = getItem(position);
            title.setText(item.title);
            writer.setText(item.writer);
            date.setText(item.date);
            content.setText(item.content);

            return convertView;
        }

        public void add(int position, BoardItem item) {
            itemList.add(position, item);
            notifyDataSetChanged();
        }
    }

    private class RequestBoardContentTask extends RequestBaseTask {

        OnResultListener listener;
        String contentUrl;
        int position;

        public RequestBoardContentTask(int position, String contentUrl, OnResultListener listener) {
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
                item.content = contentBuffer.toString();

                if (item != null) {
                    Logger.d(TAG, "add item : " + item.toString());
                    int res = DBManager.getInstance(getActivity()).insertThankShare(item);
                    Logger.i(TAG, "insert to local database result : " + res);
                }

                listener.onResult(item, position);

            } else {
                Logger.e(TAG, "source is null");
                listener.onResult(null, OnResultListener.POSITION_NONE);
            }
        }
    }
}
