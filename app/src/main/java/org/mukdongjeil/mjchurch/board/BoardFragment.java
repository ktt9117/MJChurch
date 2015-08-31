package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.BoardItem;
import org.mukdongjeil.mjchurch.common.dao.GalleryItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestListTask;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPageNo = 1;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        new RequestListTask(BOARD_TYPE_THANKS_SHARING, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }

                if (obj != null && obj instanceof List) {
                    List<BoardItem> itemList = (List) obj;
                    BoardListAdapter adapter = new BoardListAdapter(getActivity(), itemList);
                    setListAdapter(adapter);
                }
            }
        });
    }

    private class BoardListAdapter extends ArrayAdapter<BoardItem> {

        public BoardListAdapter(Context context, List<BoardItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_list_board, null);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView writer = (TextView) convertView.findViewById(R.id.writer);
            TextView date = (TextView) convertView.findViewById(R.id.date);

            BoardItem item = getItem(position);
            title.setText(item.title);
            writer.setText(item.writer);
            date.setText(item.date);

            return convertView;
        }
    }
}
