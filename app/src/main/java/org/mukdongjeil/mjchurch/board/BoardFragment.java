package org.mukdongjeil.mjchurch.board;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.htmlparser.jericho.Element;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.board.adapters.BoardListAdapter;
import org.mukdongjeil.mjchurch.common.dao.BoardItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.database.DBManager;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestBoardContentTask;
import org.mukdongjeil.mjchurch.protocol.RequestListTask;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-08-25.
 */
public class BoardFragment extends Fragment {
    private static final String TAG = BoardFragment.class.getSimpleName();

    public static final int BOARD_TYPE_THANKS_SHARING = 17;
    public static final int BOARD_TYPE_GALLERY = 18;
    public static final int BOARD_TYPE_NEW_PERSON = 19;

    private int mPageNo;
    private List<BoardItem> mItemList;
    private List<BoardItem> mLocalItemList;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.layout_recyclerview, null);

        mLocalItemList = DBManager.getInstance(SystemHelpers.getApplicationContext()).getThankShareList();
        mItemList = new ArrayList<BoardItem>();

        // use a linear layout manager
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new BoardListAdapter(mItemList);
        mRecyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.i(TAG, "onActivityCreated");
        mPageNo = 1;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        getActivity().setTitle(MenuListFragment.BOARD_MENUS[0]);

        new RequestListTask(BOARD_TYPE_THANKS_SHARING, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {

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
                            mItemList.add(localItem);
                            mAdapter.notifyDataSetChanged();
                        } else {
                            new RequestBoardContentTask(getActivity(), i, href, new RequestBaseTask.OnResultListener() {
                                @Override
                                public void onResult(Object obj, int position) {
                                    if (obj != null && obj instanceof BoardItem) {
                                        if (position == POSITION_NONE) {
                                            position = 0;
                                        }
                                        mItemList.add(position, (BoardItem) obj);
                                        mAdapter.notifyDataSetChanged();
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
}