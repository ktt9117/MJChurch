package org.mukdongjeil.mjchurch.introduce;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.HTMLElements;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.util.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class IntroduceFragment extends Fragment {
    private static final String TAG = IntroduceFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_introduce, container, false);
        ImageView imgView = (ImageView) rootView.findViewById(R.id.imgView);
        imgView.setImageResource(R.mipmap.introduce_contents);
        return rootView;
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
                //Logger.i(TAG, "source : " + source);
                StringBuffer sb = new StringBuffer();
                List<Element> elements = source.getAllElements();
                boolean pass = false;
                for (Element element : elements) {
                    StartTag sTag = element.getStartTag();
                    //Logger.i(TAG, "start tag : " + tag.toString());
                    if (sTag != null && sTag.toString().contains(HTMLElementName.DIV)) {
                        if (sTag.toString().contains("sub_top_menu_01")
                                || sTag.toString().contains("sub_top_menu_02")
                                || sTag.toString().contains("sub_top_menu_03")
                                || sTag.toString().contains("h2_box")
                                || sTag.toString().contains("footer")) {
                            pass = true;
                        }
                    }

                    if (pass == false) {
                        sb.append(element.toString());
                    } else {
                        Logger.i(TAG, "pass elements : " + element.toString());
                    }


                    EndTag eTag = element.getEndTag();
                    if (eTag != null && eTag.toString().contains(HTMLElementName.DIV)) {
                        if (sTag.toString().contains("sub_top_menu_01")
                                || sTag.toString().contains("sub_top_menu_02")
                                || sTag.toString().contains("sub_top_menu_03")
                                || sTag.toString().contains("h2_box")
                                || sTag.toString().contains("footer")) {
                            pass = false;
                        }
                    }
                }
            }
        }
    }
}
