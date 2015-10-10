package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.GalleryItem;
import org.mukdongjeil.mjchurch.common.util.DisplayUtil;
import org.mukdongjeil.mjchurch.common.util.ExHandler;
import org.mukdongjeil.mjchurch.common.util.Logger;
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
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPageNo = 0;
        hasMorePage = true;

        mBoardType = (getArguments() != null) ? getArguments().getInt(MenuListFragment.SELECTED_MENU_INDEX) : BoardFragment.BOARD_TYPE_GALLERY;

        mItemList = new ArrayList<>();
        mAdapter = new BoardGridAdapter(mItemList);
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

    private class BoardGridAdapter extends BaseAdapter {

        private List<GalleryItem> list;

        public BoardGridAdapter(List<GalleryItem> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public GalleryItem getItem(int position) {
            return list != null ? list.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final GridViewHolder holder;
            View v;
            if (convertView == null) {
                holder = new GridViewHolder();
                v = makeGridRowView(holder, getActivity());
            } else {
                holder = (GridViewHolder) convertView.getTag();
                v = holder.layout;
            }

            final GalleryItem item = getItem(position);
            if (item != null) {
                if (!TextUtils.isEmpty(item.photoUrl)) {
                    Logger.d(TAG, "gridview > getView photoUrl : " + item.photoUrl);
                    item.photoUrl = item.photoUrl.replaceAll("&amp;", "&");
                    if (!item.photoUrl.contains("http")) {
                        item.photoUrl = Const.BASE_URL + item.photoUrl;
                    }
                    ImageLoader.getInstance().loadImage(item.photoUrl, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {}

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {}

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            //Logger.e(TAG, "onLoadingComplete s : " + s + ", bitmap : " + bitmap);
                            if (holder != null && holder.imageView != null) {
                                holder.imageView.setImageBitmap(bitmap);
                            }
                        }
                        @Override
                        public void onLoadingCancelled(String s, View view) {}
                    });
                }
                if (holder.textView != null) {
                    holder.textView.setText(item.title);
                }
            }
            return v;
        }
    }

    public static class GridViewHolder {
        public View layout;
        public ImageView imageView;
        public TextView textView;
    }

    private View makeGridRowView(GridViewHolder holder, Context context) {
        int displayWidth = DisplayUtil.getDisplaySizeWidth(getActivity());
        int imageViewWidth = displayWidth / 3;

        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));

        ImageView imageView = new ImageView(context);
        imageView.setId((int) System.currentTimeMillis());
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(imageViewWidth, imageViewWidth));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView);
        holder.imageView = imageView;

        TextView textView = new TextView(context);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(imageViewWidth, (imageViewWidth / 3));
        tvParams.addRule(RelativeLayout.BELOW, imageView.getId());
        textView.setLayoutParams(tvParams);
        textView.setSingleLine(true);
        textView.setGravity(Gravity.CENTER);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(5, 5, 5, 5);
        layout.addView(textView);

        holder.textView = textView;
        holder.layout = layout;

        layout.setTag(holder);
        return layout;
    }
}