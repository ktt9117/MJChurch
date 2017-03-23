package org.mukdongjeil.mjchurch.sermon;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.service.MediaService;

import java.util.ArrayList;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class SermonListAdapter extends RecyclerView.Adapter<SermonListAdapter.ViewHolder> {
    private static final String TAG = SermonListAdapter.class.getSimpleName();

    private Context context;
    private MediaService service;
    private ArrayList<SermonItem> itemList;
    private int selectedItemPosition = -1;
    private OnSermonItemClickListener listener;

    public SermonListAdapter(Context context, MediaService service, ArrayList<SermonItem> items, OnSermonItemClickListener listener) {
        this.context = context;
        this.service = service;
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
        return new ViewHolder((ViewGroup)v, context, service);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final SermonItem item = itemList.get(position);
        holder.item = item;
        holder.titleWithDate.setText(item.titleWithDate);
        holder.preacher.setText(item.preacher);
        holder.playInfo.setText("");
//        holder.content.setText(item.content);
        if (item.downloadStatus.name().equals(SermonItem.DownloadStatus.DOWNLOAD_SUCCESS.name())) {
            holder.downloadInfo.setText("다운로드 됨");
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
                    listener.onSermonItemClicked(item);
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

    public void setCurrentItemSelected(int position) {
        selectedItemPosition = position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout layout;
        ImageView imgView;
        TextView titleWithDate;
        TextView preacher;
        TextView chapterInfo;
        //TextView content;
        //TextView btnMore;
        TextView playInfo;
        TextView downloadInfo;
        MediaService player;
        SermonItem item;

        public ViewHolder(ViewGroup v, final Context context, MediaService service) {
            super(v);
            layout = (RelativeLayout) v.findViewById(R.id.row_sermon_layout);
            imgView = (ImageView) v.findViewById(R.id.img_preacher);
            titleWithDate = (TextView) v.findViewById(R.id.title_with_date);
            preacher = (TextView) v.findViewById(R.id.preacher);
            chapterInfo = (TextView) v.findViewById(R.id.chapter_info);
            //content = (TextView) v.findViewById(R.id.content_summary);
            playInfo = (TextView) v.findViewById(R.id.play_info_text);
            downloadInfo = (TextView) v.findViewById(R.id.download_info);
            player = service;

//            btnMore = (TextView) v.findViewById(R.id.content_more);
//            btnMore.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Toast.makeText(context, "구현 준비중 입니다.", Toast.LENGTH_SHORT).show();
//                }
//            });

        }
    }

    public interface OnSermonItemClickListener {
        void onSermonItemClicked(SermonItem item);
    }
}