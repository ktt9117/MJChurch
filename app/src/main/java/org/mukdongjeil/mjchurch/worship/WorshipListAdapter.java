package org.mukdongjeil.mjchurch.worship;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.service.MediaService;

import java.io.IOException;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class WorshipListAdapter extends ArrayAdapter<SermonItem> {
    private static final String TAG = WorshipListAdapter.class.getSimpleName();

    private MediaService service;

    public WorshipListAdapter(Context context, MediaService service) {
        super(context, 0);
        this.service = service;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_sermon, null, false);
            vh = new ViewHolder(getContext(), convertView, service);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        SermonItem item = getItem(position);
        vh.item = item;
        vh.title.setText(item.title);
        vh.date.setText(item.date);
        vh.preacher.setText(item.preacher);
        vh.chapterInfo.setText(item.chapterInfo);
        if (!TextUtils.isEmpty(item.preacher) && item.preacher.contains("김희준")) {
            vh.imgView.setImageResource(R.mipmap.preacher_heejun);
        } else {
            vh.imgView.setImageResource(R.mipmap.ic_launcher);
        }
        vh.btnPlay.setTag(Const.BASE_URL + item.audioFilePath);

        return convertView;
    }

    public class ViewHolder {
        ImageView imgView;
        TextView title;
        TextView date;
        TextView preacher;
        TextView chapterInfo;
        TextView content;
        Button btnPlay;
        Button btnDownload;
        Button btnStop;
        MediaService player;
        SermonItem item;

        public ViewHolder(final Context context, View rootView, MediaService service) {
            imgView = (ImageView) rootView.findViewById(R.id.img_preacher);
            title = (TextView) rootView.findViewById(R.id.title);
            date = (TextView) rootView.findViewById(R.id.date);
            preacher = (TextView) rootView.findViewById(R.id.preacher);
            chapterInfo = (TextView) rootView.findViewById(R.id.chapter_info);
            content = (TextView) rootView.findViewById(R.id.content_summary);
            btnPlay = (Button) rootView.findViewById(R.id.btn_play);
            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item != null && !TextUtils.isEmpty(item.audioFilePath)) {
                        if (player != null) {
                            try {
                                player.startPlayer(item);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Logger.e(TAG, "Media play failed caused by media player is not bounded yet maybe");
                        }
                    } else {
                        Toast.makeText(context, "오디오 경로가 없거나 잘 못 되었습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
            btnStop = (Button) rootView.findViewById(R.id.btn_stop);
            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (player != null) {
                        player.stopPlayer();
                    } else {
                        Logger.e(TAG, "Media stop failed caused by media player is not bounded yet maybe");
                    }
                }
            });
            btnDownload = (Button) rootView.findViewById(R.id.btn_download);
            btnDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "구현 예정입니다.", Toast.LENGTH_SHORT).show();
                }
            });
            player = service;
        }
    }
}