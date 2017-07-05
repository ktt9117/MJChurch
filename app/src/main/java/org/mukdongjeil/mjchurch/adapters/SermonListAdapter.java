package org.mukdongjeil.mjchurch.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.models.DownloadStatus;
import org.mukdongjeil.mjchurch.models.Sermon;

import java.util.ArrayList;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class SermonListAdapter extends RecyclerView.Adapter<SermonListAdapter.ViewHolder> {
    private static final String TAG = SermonListAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout layout;
        ImageView imgView;
        TextView titleWithDate;
        TextView preacher;
        TextView chapterInfo;
        TextView playInfo;
        TextView downloadInfo;
        Sermon item;

        public ViewHolder(ViewGroup v) {
            super(v);
            layout = (RelativeLayout) v.findViewById(R.id.row_sermon_layout);
            imgView = (ImageView) v.findViewById(R.id.img_preacher);
            titleWithDate = (TextView) v.findViewById(R.id.title_with_date);
            preacher = (TextView) v.findViewById(R.id.preacher);
            chapterInfo = (TextView) v.findViewById(R.id.chapter_info);
            playInfo = (TextView) v.findViewById(R.id.play_info_text);
            downloadInfo = (TextView) v.findViewById(R.id.download_info);
        }
    }

    public interface OnSermonClickListener {
        void onSermonClicked(Sermon item);
    }

    private ArrayList<Sermon> itemList;
    private OnSermonClickListener listener;

    public SermonListAdapter(ArrayList<Sermon> items, OnSermonClickListener listener) {
        this.itemList = items;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return (itemList != null) ? itemList.size() : 0;
    }

    @Override
    public SermonListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sermon, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder((ViewGroup)v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Sermon item = itemList.get(position);
        holder.item = item;
        holder.titleWithDate.setText(item.titleWithDate);
        holder.preacher.setText(item.preacher);
        holder.playInfo.setText("");
//        holder.content.setText(item.content);
        if (DownloadStatus.parse(item.downloadStatus) == DownloadStatus.DOWNLOAD_SUCCESS) {
            holder.downloadInfo.setText("다운로드 됨");
        } else if (DownloadStatus.parse(item.downloadStatus) == DownloadStatus.DOWNLOAD_START){
            holder.downloadInfo.setText("다운로드 중");
        } else {
            holder.downloadInfo.setText("");
        }

        holder.chapterInfo.setText(item.chapterInfo);
        if (!TextUtils.isEmpty(item.preacher) && item.preacher.contains("김희준")) {
            holder.imgView.setImageResource(R.drawable.preacher_heejun);
        } else {
            holder.imgView.setImageResource(R.mipmap.ic_launcher);
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSermonClicked(item);
                }
            }
        });

        //TODO : check this later
//        if (selectedItemPosition == position) {
//            convertView.setBackgroundColor(Color.LTGRAY);
//        } else {
//            convertView.setBackgroundColor(Color.WHITE);
//        }
    }
}