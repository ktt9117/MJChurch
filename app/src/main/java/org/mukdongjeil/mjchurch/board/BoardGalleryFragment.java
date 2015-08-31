package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.GalleryItem;
import org.mukdongjeil.mjchurch.common.util.DisplayUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestListTask;

import java.util.List;

/**
 * Created by Kim SungJoong on 2015-08-31.
 */
public class BoardGalleryFragment extends Fragment {
    private static final String TAG = BoardGalleryFragment.class.getSimpleName();

    private GridView mGridView;
    private int mPageNo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_grid_board, null);
        mGridView = (GridView) v.findViewById(R.id.gridview);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPageNo = 1;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        new RequestListTask(BoardFragment.BOARD_TYPE_GALLERY, mPageNo, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }
                List<GalleryItem> itemList = (List) obj;
                BoardGridAdapter adapter = new BoardGridAdapter(itemList);
                mGridView.setAdapter(adapter);
            }
        });
    }

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
                    ImageLoader.getInstance().loadImage(Const.BASE_URL + item.photoUrl, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            //Logger.e(TAG, "onLoadingComplete s : " + s + ", bitmap : " + bitmap);
                            if (holder.imageView != null) {
                                holder.imageView.setImageBitmap(bitmap);
                            }
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {
                        }
                    });
                }
                if (holder.textView != null) {
                    holder.textView.setText(item.title);
                }
            }
            return v;
        }
    }

    public class GridViewHolder {
        public View layout;
        public ImageView imageView;
        public TextView textView;
    }

    private View makeGridRowView(GridViewHolder holder, Context context) {
        int displayWidth = DisplayUtil.getDisplaySizeWidth(getActivity());
        //Logger.d(TAG, "displayWidth : " + displayWidth);
        int imageViewWidth = displayWidth / 3;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new AbsListView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));

        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(imageViewWidth, imageViewWidth));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView);
        holder.imageView = imageView;

        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(imageViewWidth, (imageViewWidth / 3)));
        textView.setMaxLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(10, 10, 10, 10);
        layout.addView(textView);

        holder.textView = textView;
        holder.layout = layout;

        layout.setTag(holder);
        return layout;
    }
}
