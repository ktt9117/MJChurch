package org.mukdongjeil.mjchurch.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wang.avi.AVLoadingIndicatorView;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.utils.CommonUtils;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.models.DownloadStatus;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.services.MediaService;

import java.util.ArrayList;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class SermonListAdapter extends RecyclerView.Adapter<SermonListAdapter.ViewHolder> {
    private static final String TAG = SermonListAdapter.class.getSimpleName();

    private ArrayList<Sermon> mList;
    private OnRowButtonClickListener mOnRowButtonClickListener;
    private RequestManager mGlide;

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

    public SermonListAdapter(ArrayList<Sermon> items, OnRowButtonClickListener listener, RequestManager glide) {
        this.mList = items;
        this.mOnRowButtonClickListener = listener;
        this.mGlide = glide;
    }

    @Override
    public int getItemCount() {
        return (mList != null) ? mList.size() : 0;
    }

    @Override
    public SermonListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sermon, parent, false);
        return new ViewHolder((ViewGroup) v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Sermon item = mList.get(position);
        final int index = position;
        holder.item = item;
        holder.titleWithDate.setText(item.titleWithDate);
        holder.btnDownload.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(item.preacher) && item.preacher.contains("김희준")) {
            holder.imgPreacher.setImageResource(R.drawable.preacher_heejun);
        } else {
            if (item.mediaType == Const.MEDIA_TYPE_VIDEO) {
                holder.btnDownload.setVisibility(View.GONE);
                mGlide.load(CommonUtils.getYoutubeThumbnailUrl(item.videoUrl))
                        .placeholder(R.drawable.preacher_heejun)
                        .error(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imgPreacher);
            } else {
                holder.imgPreacher.setImageResource(R.mipmap.ic_launcher);
            }
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
                if (mOnRowButtonClickListener != null) {
                    mOnRowButtonClickListener.onPlayClicked(item, index);
                }
            }
        });

        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logger.e(TAG, "btnDownload clicked");
                if (mOnRowButtonClickListener != null) {
                    mOnRowButtonClickListener.onDownloadClicked(item, index);
                }
            }
        });
    }
}