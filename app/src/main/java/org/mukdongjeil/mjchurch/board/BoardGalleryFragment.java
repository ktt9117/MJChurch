package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.board.adapters.BoardGridAdapter;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.GalleryItem;
import org.mukdongjeil.mjchurch.common.util.DisplayUtil;
import org.mukdongjeil.mjchurch.common.util.ExHandler;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestListTask;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-08-31.
 */
public class BoardGalleryFragment extends Fragment {
    private static final String TAG = BoardGalleryFragment.class.getSimpleName();

    private static final int HANDLE_WHAT_GET_CONTENTS = 100;

    private int mBoardType;
    private GridView mGridView;
    private BoardGridAdapter mAdapter;
    private int mPageNo;
    private List<GalleryItem> mItemList;
    private boolean hasMorePage;
    private boolean isDetached = false;
    private int mColumnWidth;

    private ExHandler<BoardGalleryFragment> mHandler = new ExHandler<BoardGalleryFragment>(this) {
        @Override
        protected void handleMessage(BoardGalleryFragment reference, Message msg) {
            if (isDetached) return;
            mPageNo++;
            new RequestListTask(mBoardType, mPageNo, new RequestBaseTask.OnResultListener() {
                @Override
                public void onResult(Object obj, int position) {
                    if (isDetached) return;

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).hideLoadingDialog();
                    }

                    if (obj != null && obj instanceof List) {
                        List<GalleryItem> list = (List<GalleryItem>) obj;
                        mItemList.addAll(list);
                        mAdapter.notifyDataSetChanged();
                        if (list.size() < Const.GALLERY_LIST_COUNT_PER_PAGE) {
                            hasMorePage = false;
                        }
                    } else {
                        hasMorePage = false;
                    }

                    if (hasMorePage == true) {
                        mHandler.sendEmptyMessage(HANDLE_WHAT_GET_CONTENTS);
                    }
                }
            });
        }
    } ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grid_board, null);
        mGridView = (GridView) v.findViewById(R.id.gridview);

        int displayWidth = DisplayUtil.getDisplaySizeWidth(getActivity());
        mColumnWidth = displayWidth / 3;
        mGridView.setColumnWidth(mColumnWidth);
        mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPageNo = 0;
        hasMorePage = true;

        mBoardType = (getArguments() != null) ? getArguments().getInt(MenuListFragment.SELECTED_MENU_INDEX) : BoardFragment.BOARD_TYPE_GALLERY;

        String title;
        if (mBoardType == BoardFragment.BOARD_TYPE_GALLERY) {
            title = MenuListFragment.BOARD_MENUS[1];
        } else {
            title = MenuListFragment.BOARD_MENUS[2];
        }
        getActivity().setTitle(title);
        /*
        if (mBoardType == BoardFragment.BOARD_TYPE_GALLERY) {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("게시판")
                    .putContentType("사진 조회")
                    .putContentId("교회앨범"));
        } else {
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("게시판")
                    .putContentType("사진 조회")
                    .putContentId("새신자앨범"));
        }
        */

        mItemList = new ArrayList<GalleryItem>();
        mAdapter = new BoardGridAdapter(getActivity(), mItemList, mColumnWidth);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(mOnGridItemClickListener);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        mHandler.sendEmptyMessage(HANDLE_WHAT_GET_CONTENTS);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isDetached = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeMessages(HANDLE_WHAT_GET_CONTENTS);
        isDetached = true;
        hasMorePage = false;
    }

    private AdapterView.OnItemClickListener mOnGridItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            GalleryItem item = mItemList.get(position);
            Fragment newFragment = BoardGalleryDetailFragment.newInstance(mBoardType, item.bbsNo);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).addContent(newFragment);
            }
        }
    };
}