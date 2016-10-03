package org.mukdongjeil.mjchurch.board.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.GalleryItem;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.util.List;

/**
 * Created by gradler on 2016. 9. 30..
 */
public class BoardGridAdapter extends BaseAdapter {
    private static final String TAG = BoardGridAdapter.class.getSimpleName();

    private Context context;
    private List<GalleryItem> list;
    private int columnWidth;

    public BoardGridAdapter(Context context, List<GalleryItem> list, int columnWidth) {
        this.context = context;
        this.list = list;
        this.columnWidth = columnWidth;
    }

    @Override
    public int getCount() {
        return (list != null) ? list.size() : 0;
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
            v = makeGridRowView(holder, context);
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

                Glide.with(context)
                        .load(item.photoUrl)
                        .placeholder(Const.DEFAULT_IMG_RESOURCE)
                        .crossFade()
                        .into(holder.imageView);

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
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(imageView);
        holder.imageView = imageView;

        TextView textView = new TextView(context);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(columnWidth, AbsListView.LayoutParams.WRAP_CONTENT);
        tvParams.addRule(RelativeLayout.BELOW, imageView.getId());
        textView.setLayoutParams(tvParams);
        textView.setSingleLine(true);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(5, 5, 5, 5);
        layout.addView(textView);

        holder.textView = textView;
        holder.layout = layout;

        layout.setTag(holder);
        return layout;
    }

    public static class GridViewHolder {
        public View layout;
        public ImageView imageView;
        public TextView textView;
    }
}
