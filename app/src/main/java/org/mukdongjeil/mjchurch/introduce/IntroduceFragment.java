package org.mukdongjeil.mjchurch.introduce;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.photoview.PhotoViewAttacher;
import org.mukdongjeil.mjchurch.common.util.DisplayUtil;
import org.mukdongjeil.mjchurch.common.util.ImageUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.common.view.ImageBaseFragment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class IntroduceFragment extends ImageBaseFragment {
    private static final String TAG = IntroduceFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            new RequestTask().execute(Const.INTRODUCE_URL);
        } else {
            Logger.d(TAG, "do not request again. caused by savedInstanceState is not null");
        }
    }

    private class RequestTask extends AsyncTask<String, Void, Source> {

        @Override
        protected Source doInBackground(String... params) {
            if (params == null && params[0] == null) {
                return null;
            }
            try {
                URL url = new URL(params[0]);
                return new Source(url);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Source source) {
            super.onPostExecute(source);
            if (source != null) {
                Element tab1 = source.getElementById("tab1");
                if (tab1 != null) {
                    Element imgElement = tab1.getFirstElement(HTMLElementName.IMG);
                    String src = imgElement.getAttributeValue("src");
                    src = src.replaceAll("&amp;", "&");
                    ImageLoader.getInstance().loadImage(Const.BASE_URL + src, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            Bitmap resizedBitmap = ImageUtil.getResizeBitmapImage(bitmap, DisplayUtil.getDisplaySizeWidth(SystemHelpers.getApplicationContext()));
                            getImageView().setImageBitmap(resizedBitmap);
                            new PhotoViewAttacher(getImageView());
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {

                        }
                    });
                } else {
                    Logger.e(TAG, "tab1 element is null");
                }
            }
        }
    }
}
