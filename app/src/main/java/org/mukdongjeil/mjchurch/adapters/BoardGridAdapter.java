package org.mukdongjeil.mjchurch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.models.Gallery;
import org.mukdongjeil.mjchurch.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by gradler on 2016. 9. 30..
 */
public class BoardGridAdapter extends BaseAdapter {
    private static final String TAG = BoardGridAdapter.class.getSimpleName();

    private Context mContext;
    private RequestManager mRequestManager;
    private List<Gallery> mList;
    private int mColumnWidth;
    private int count = 0;

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
            Logger.e(TAG, "item : photoUrl : " + item.photoUrl);
            if (!TextUtils.isEmpty(item.photoUrl)) {
//                mRequestManager.load(item.photoUrl).placeholder(Const.DEFAULT_IMG_RESOURCE)
//                        .crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.imageView);

                AsyncHttpClient httpClient = new AsyncHttpClient();
                String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/tempfile" + count + ".jpg";
                count++;
                File file = new File(filename);
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                httpClient.get(item.photoUrl, new FileAsyncHttpResponseHandler(file) {
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                        Logger.e(TAG, "onFailure statusCode : " + statusCode);

                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, File file) {
                        Logger.e(TAG, "onSuccess statusCode : " + statusCode);
                    }
                });
                //new DownloadTask(mContext).execute(item.photoUrl);
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

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            /*
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                Logger.e(TAG, "DownloadTask doInBackground url : " + sUrl[0]);
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/tempfile" + count + ".jpg";
                count++;
                File file = new File(filename);
                file.createNewFile();
                output = new FileOutputStream(filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    Logger.e(TAG, "total : " + total);
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                    output.flush();
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            */
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Logger.i(TAG, "DownloadTask onPostExecute : " + s);
        }
    }
}