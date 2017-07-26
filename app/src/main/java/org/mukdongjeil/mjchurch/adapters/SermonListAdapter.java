package org.mukdongjeil.mjchurch.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.models.DownloadStatus;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.service.MediaService;

import java.util.ArrayList;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class SermonListAdapter extends RecyclerView.Adapter<SermonListAdapter.ViewHolder> {
    private static final String TAG = SermonListAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPreacher;
        TextView titleWithDate;
        TextView downloadPercent;
        ImageView btnPlay;
        ImageView btnDownload;
        Sermon item;
        AVLoadingIndicatorView progressBar;

        public ViewHolder(ViewGroup v) {
            super(v);
            imgPreacher = (ImageView) v.findViewById(R.id.row_img_preacher);
            titleWithDate = (TextView) v.findViewById(R.id.row_title_with_date);
            downloadPercent = (TextView) v.findViewById(R.id.row_download_percent);
            btnPlay = (ImageView) v.findViewById(R.id.row_btn_play);
            btnDownload = (ImageView) v.findViewById(R.id.row_btn_download);
            progressBar = (AVLoadingIndicatorView) v.findViewById(R.id.row_progress_bar);
        }
    }

    public interface OnRowButtonClickListener {
        void onPlayClicked(Sermon item, int position);

        void onDownloadClicked(Sermon item, int position);
    }

    public int downloadPercent = -1;

    private ArrayList<Sermon> itemList;
    private OnRowButtonClickListener onRowButtonClickListener;

    public SermonListAdapter(ArrayList<Sermon> items, OnRowButtonClickListener listener) {
        this.itemList = items;
        this.onRowButtonClickListener = listener;
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
        return new ViewHolder((ViewGroup) v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Sermon item = itemList.get(position);
        final int index = position;
        holder.item = item;
        holder.titleWithDate.setText(item.titleWithDate);

        if (!TextUtils.isEmpty(item.preacher) && item.preacher.contains("김희준")) {
            holder.imgPreacher.setImageResource(R.drawable.preacher_heejun);
        } else {
            holder.imgPreacher.setImageResource(R.mipmap.ic_launcher);
        }

        switch (item.playStatus) {
            case MediaService.PLAY_STATUS_PLAY:
                holder.btnPlay.setImageResource(R.drawable.ic_pause_small);
                holder.progressBar.smoothToShow();
                break;
            default:
                holder.btnPlay.setImageResource(R.drawable.ic_play_small);
                holder.progressBar.hide();
                break;
        }

        switch (DownloadStatus.parse(item.downloadStatus)) {
            case START:
                if (item.downloadPercent > -1) {
                    holder.downloadPercent.setText(String.format("다운로드 중 %d", item.downloadPercent) + "%");
                }
                holder.btnDownload.setImageResource(R.drawable.ic_download_black);
                holder.btnDownload.setEnabled(false);
                break;
            case COMPLETE:
                holder.downloadPercent.setText(R.string.download_done);
                holder.btnDownload.setImageResource(R.drawable.ic_download_done);
                holder.btnDownload.setEnabled(false);
                break;
            default:
                holder.downloadPercent.setText(null);
                holder.btnDownload.setImageResource(R.drawable.ic_download_black);
                holder.btnDownload.setEnabled(true);
                break;
        }

        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.e(TAG, "btnPlay clicked");
                if (onRowButtonClickListener != null) {
                    onRowButtonClickListener.onPlayClicked(item, index);
                }
            }
        });

        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.e(TAG, "btnDownload clicked");
                if (onRowButtonClickListener != null) {
                    onRowButtonClickListener.onDownloadClicked(item, index);
                }
            }
        });
    }
}