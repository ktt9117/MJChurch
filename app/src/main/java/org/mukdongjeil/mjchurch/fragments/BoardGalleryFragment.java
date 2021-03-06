package org.mukdongjeil.mjchurch.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bumptech.glide.Glide;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.adapters.BoardGridAdapter;
import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.utils.DisplayUtil;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.PreferenceUtil;
import org.mukdongjeil.mjchurch.models.Gallery;
import org.mukdongjeil.mjchurch.protocols.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocols.RequestListTask;
import org.mukdongjeil.mjchurch.services.DataService;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Kim SungJoong on 2015-08-31.
 */
public class BoardGalleryFragment extends LoadingMenuBaseFragment {
    private static final String TAG = BoardGalleryFragment.class.getSimpleName();

    private static final int HANDLE_WHAT_GET_CONTENTS = 100;

    private int mBoardType;
    private GridView mGridView;
    private BoardGridAdapter mAdapter;
    private int mPageNo;
    private List<Gallery> mItemList;
    private boolean mHasMorePage;
    private boolean mIsDetached = false;
    private int mColumnWidth;

    private Realm mRealm;

    final private ExHandler<BoardGalleryFragment> mHandler = new ExHandler<BoardGalleryFragment>(this) {
        @Override
        protected void handleMessage(BoardGalleryFragment reference, Message msg) {
            if (mIsDetached) return;

            showActionBarProgress();
            mPageNo++;
            new RequestListTask(mBoardType, mPageNo, new RequestBaseTask.OnResultListener() {
                @Override
                public void onResult(Object obj, int position) {
                    if (mBoardType == BoardFragment.BOARD_TYPE_GALLERY) {
                        PreferenceUtil.setGalleryListCheckTimeInMillis(System.currentTimeMillis());
                    } else {
                        PreferenceUtil.setGalleryNewPersonListTimeInMillis(System.currentTimeMillis());
                    }

                    if (mIsDetached) return;

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).hideLoadingDialog();
                    }

                    if (obj != null && obj instanceof List) {
                        final List<Gallery> list = (List<Gallery>) obj;
                        for (Gallery gallery : list) {
                            Gallery localExistsGallery = DataService.getGallery(mRealm, gallery.photoUrl);
                            if (localExistsGallery == null) {
                                DataService.insertToRealm(mRealm, gallery);
                            }
                        }

                        mItemList.addAll(list);
                        mAdapter.notifyDataSetChanged();
                        if (list.size() < Const.GALLERY_LIST_COUNT_PER_PAGE) {
                            mHasMorePage = false;
                        }
                    } else {
                        mHasMorePage = false;
                    }

                    if (mHasMorePage) {
                        mHandler.sendEmptyMessage(HANDLE_WHAT_GET_CONTENTS);
                    }

                    hideActionBarProgressDelayed(2000);
                }
            });
        }
    };

    public BoardGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grid_board, container, false);
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
        mHasMorePage = true;

        mBoardType = (getArguments() != null) ?
                getArguments().getInt(Const.INTENT_KEY_SELECTED_MENU_INDEX) : BoardFragment.BOARD_TYPE_GALLERY;

        mItemList = new ArrayList<>();
        mAdapter = new BoardGridAdapter(getActivity(), Glide.with(this), mItemList, mColumnWidth);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(mOnGridItemClickListener);

        loadList();
    }

    private boolean isAlreadyCheckToday() {
        long lastCheckTimeInMillis = mBoardType == BoardFragment.BOARD_TYPE_GALLERY ?
                PreferenceUtil.getGalleryListCheckTimeInMillis() : PreferenceUtil.getGalleryNewPersonListTimeInMillis();
        if (lastCheckTimeInMillis > 0) {
            long oneDayGap = System.currentTimeMillis() - Const.DAY_IN_MILLIS;
            return lastCheckTimeInMillis > oneDayGap;
        }

        return false;
    }

    private void loadList() {
        RealmResults<Gallery> results = DataService.getGalleryList(mRealm, mBoardType);
        if (results.size() > 0) {
            mItemList.addAll(results);

            if (isAlreadyCheckToday()) {
                return;
            }
        }

        mHandler.sendEmptyMessage(HANDLE_WHAT_GET_CONTENTS);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mIsDetached = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeMessages(HANDLE_WHAT_GET_CONTENTS);
        mIsDetached = true;
        mHasMorePage = false;
    }

    final private AdapterView.OnItemClickListener mOnGridItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Gallery item = mItemList.get(position);
            Fragment newFragment = BoardGalleryDetailFragment.newInstance(mBoardType, item.bbsNo);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).addContent(newFragment);
            }
        }
    };
}