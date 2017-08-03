package org.mukdongjeil.mjchurch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.models.Gallery;

import java.util.List;

/**
 * Created by gradler on 2016. 9. 30..
 */
public class BoardGridAdapter extends BaseAdapter {
    private static final String TAG = BoardGridAdapter.class.getSimpleName();

    private Context mContext;
    private RequestManager mRequestManager;
    private List<Gallery> mList;
    private int mColumnWidth;

    public static class GridViewHolder {
        public View layout;
        public ImageView imageView;
        public TextView textView;
    }

    public BoardGridAdapter(Context context, RequestManager requestManager, List<Gallery> list, int columnWidth) {
        this.mContext = context;
        this.mRequestManager = requestManager;
        this.mList = list;
        this.mColumnWidth = columnWidth;
    }

    @Override
    public int getCount() {
        return (mList != null) ? mList.size() : 0;
    }

    @Override
    public Gallery getItem(int position) {
        return mList != null ? mList.get(position) : null;
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
            v = makeGridRowView(holder, mContext);
        } else {
            holder = (GridViewHolder) convertView.getTag();
            v = holder.layout;
        }

        final Gallery item = getItem(position);
        if (item != null) {
            if (!TextUtils.isEmpty(item.photoUrl)) {
                mRequestManager.load(item.photoUrl).placeholder(Const.DEFAULT_IMG_RESOURCE)
                        .crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imageView);
            }
            if (holder.textView != null) {
                holder.textView.setText(item.title);
            }
        }
        return v;
    }

    private View makeGridRowView(GridViewHolder holder, Context context) {

        RelativeLayout layout = new RelativeLayout(context);
        layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));

        ImageView imageView = new ImageView(context);
        imageView.setId(R.id.grid_img);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(mColumnWidth, mColumnWidth));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView);
        holder.imageView = imageView;

        TextView textView = new TextView(context);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(mColumnWidth, AbsListView.LayoutParams.WRAP_CONTENT);
        tvParams.addRule(RelativeLayout.ALIGN_BOTTOM, imageView.getId());
        textView.setLayoutParams(tvParams);
        textView.setSingleLine(true);
        textView.setTextColor(Color.parseColor("#dedede"));
        textView.setGravity(Gravity.CENTER);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(5, 5, 5, 5);
        textView.setBackgroundColor(Color.parseColor("#88000000"));
        layout.addView(textView);

        holder.textView = textView;
        holder.layout = layout;

        layout.setTag(holder);
        return layout;
    }
}
