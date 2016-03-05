package org.mukdongjeil.mjchurch.sermon;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.dao.SermonItem;
import org.mukdongjeil.mjchurch.service.MediaService;

/**
 * Created by Kim SungJoong on 2015-08-20.
 */
public class SermonListAdapter extends ArrayAdapter<SermonItem> {
    private static final String TAG = SermonListAdapter.class.getSimpleName();

    private MediaService service;
    private int selectedItemPosition = -1;

    public SermonListAdapter(Context context, MediaService service) {
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
        vh.playInfo.setText("");
        vh.content.setText(item.content);
        if (item.downloadStatus.name().equals(SermonItem.DownloadStatus.DOWNLOAD_SUCCESS.name())) {
            vh.downloadInfo.setText("다운로드 됨");
        } else {
            vh.downloadInfo.setText("");
        }

        vh.chapterInfo.setText(item.chapterInfo);
        if (!TextUtils.isEmpty(item.preacher) && item.preacher.contains("김희준")) {
            vh.imgView.setImageResource(R.drawable.preacher_heejun);
        } else {
            vh.imgView.setImageResource(R.mipmap.ic_launcher);
        }

        if (selectedItemPosition == position) {
            convertView.setBackgroundColor(Color.LTGRAY);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }

        return convertView;
    }

    public void setCurrentItemSelected(int position) {
        selectedItemPosition = position;
    }

    public class ViewHolder {

        ImageView imgView;
        TextView title;
        TextView date;
        TextView preacher;
        TextView chapterInfo;
        TextView content;
        TextView btnMore;
        TextView playInfo;
        TextView downloadInfo;
        MediaService player;
        SermonItem item;

        public ViewHolder(final Context context, View rootView, MediaService service) {
            imgView = (ImageView) rootView.findViewById(R.id.img_preacher);
            title = (TextView) rootView.findViewById(R.id.title);
            date = (TextView) rootView.findViewById(R.id.date);
            preacher = (TextView) rootView.findViewById(R.id.preacher);
            chapterInfo = (TextView) rootView.findViewById(R.id.chapter_info);
            content = (TextView) rootView.findViewById(R.id.content_summary);
            playInfo = (TextView) rootView.findViewById(R.id.play_info_text);
            downloadInfo = (TextView) rootView.findViewById(R.id.download_info);
            player = service;
            btnMore = (TextView) rootView.findViewById(R.id.content_more);
            btnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "구현 준비중 입니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}