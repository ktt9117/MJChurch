package org.mukdongjeil.mjchurch.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.htmlparser.jericho.Element;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.adapters.BoardListAdapter;
import org.mukdongjeil.mjchurch.models.Board;
import org.mukdongjeil.mjchurch.protocols.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocols.RequestBoardContentTask;
import org.mukdongjeil.mjchurch.protocols.RequestListTask;
import org.mukdongjeil.mjchurch.services.DataService;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.PreferenceUtil;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Kim SungJoong on 2015-08-25.
 */
public class BoardFragment extends LoadingMenuBaseFragment {
    private static final String TAG = BoardFragment.class.getSimpleName();

    public static final int BOARD_TYPE_THANKS_SHARING = 17;
    public static final int BOARD_TYPE_GALLERY = 18;
    public static final int BOARD_TYPE_NEW_PERSON = 19;

    private int mPageNo;
    private Realm mRealm;
    private RealmResults<Board> mLocalItemList;

    public BoardFragment() {
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
        Logger.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.layout_recyclerview, container, false);

        mLocalItemList = DataService.getBoardList(mRealm);
        Logger.e(TAG, "mLocalItemList count : " + mLocalItemList.size());

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = new BoardListAdapter(mLocalItemList);
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.i(TAG, "onActivityCreated");
        mPageNo = 1;
        loadBoardList();
    }

    private boolean isAlreadyCheckToday() {
        long lastCheckTimeInMillis = PreferenceUtil.getBoardListCheckTimeInMillis();
        if (lastCheckTimeInMillis > 0) {
            long oneDayGap = System.currentTimeMillis() - Const.DAY_IN_MILLIS;
            return lastCheckTimeInMillis > oneDayGap;
        }

        return false;
    }

    private void loadBoardList() {
        if (mLocalItemList.size() != 0 && isAlreadyCheckToday()) {
            return;
        }

        showActionBarProgress();

        new RequestListTask(BOARD_TYPE_THANKS_SHARING, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                if (obj != null && obj instanceof List) {
                    PreferenceUtil.setBoardListCheckTimeInMillis(System.currentTimeMillis());
                    List<Element> linkList = (List) obj;
                    for (int i = 0; i < linkList.size(); i++) {
                        Element link = linkList.get(i);
                        String href = link.getAttributeValue("href");
                        // compare between local database and server item list.
                        Board localItem = DataService.getBoard(mRealm, href);
                        if (localItem != null) {
                            Logger.e(TAG, "the item is already inside local DB");

                        } else {
                            new RequestBoardContentTask(i, href, new RequestBaseTask.OnResultListener() {
                                @Override
                                public void onResult(Object obj, int position) {
                                    if (obj != null && obj instanceof Board) {
                                        DataService.insertToRealm(mRealm, (Board) obj);
                                    }
                                    hideActionBarProgressDelayed(1500);

                                }
                            });
                        }
                    }
                }

                hideActionBarProgressDelayed(1500);
            }
        });
    }
}