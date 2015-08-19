package org.mukdongjeil.mjchurch.worship;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.service.MediaService;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class WorshipFragment extends ListFragment {
    public static final String TAG = WorshipFragment.class.getSimpleName();
    private int mPageNo;
    private int mWorshipType;

    private MediaService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaSerivce] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "[MediaSerivce] onServiceDisconnected");
            mService = null;
        }
    };

    private WorshipListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPageNo = 1;
        Intent service = new Intent(getActivity(), MediaService.class);
        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            int selectedMenuIndex = args.getInt(MenuListFragment.SELECTED_MENU_INDEX);
            switch (selectedMenuIndex) {
                case 8:
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_AFTERNOON;
                    break;
                case 9:
                    mWorshipType = Const.WORSHIP_TYPE_WEDNESDAY;
                    break;
                case 7:
                default:
                    mWorshipType = Const.WORSHIP_TYPE_SUNDAY_MORNING;
                    break;
            }
        }
        Logger.d(TAG, "worshipType : " + mWorshipType);

        mAdapter = new WorshipListAdapter(getActivity());
        new RequestTask().execute(Const.getWorshipListURL(mWorshipType, mPageNo));
        setListAdapter(mAdapter);
    }

    private class RequestTask extends AsyncTask<String, Void, Source> {

        @Override
        protected Source doInBackground(String... params) {
            if (params == null && params[0] == null) {
                return null;
            }
            try {
                URL url = new URL(params[0]);
                Logger.d(TAG, "request url : " + url.toString());
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
                Element contentElement = source.getFirstElementByClass("contents bbs_list");
                if (contentElement != null) {
                    //Logger.i(TAG, "contentElement : " + contentElement.toString());
                    ArrayList<String> bbsNoList = new ArrayList<String>();
                    List<Element> linkList = contentElement.getAllElementsByClass("list_link");
                    for (Element link : linkList) {
                        String linkAttr = link.getAttributeValue("href");
                        //Logger.i(TAG, "link : " + linkAttr);
                        if (!TextUtils.isEmpty(linkAttr)) {
                            String bbsNo = linkAttr.substring(linkAttr.lastIndexOf("=") + 1);
                            //Logger.i(TAG, "bbsNo : " + bbsNo);
                            bbsNoList.add(bbsNo);
                        }
                    }

                    if (bbsNoList.size() > 0) {
                        for (String bbsNo : bbsNoList) {
                            new ContentRequestTask().execute(Const.getWorshipContentURL(mWorshipType, mPageNo, bbsNo));
                        }
                        // [test code]
                        //new ContentRequestTask().execute(Const.getWorshipContentURL(mPageNo, bbsNoList.get(0)));
                    }

                } else {
                    Logger.e(TAG, "contentElement is null");
                    Logger.i(TAG, "source : " + source.toString());
                }
            } else {
                Logger.e(TAG, "source is null");
            }
        }
    }

    public class ContentRequestTask extends AsyncTask<String, Void, Source> {
        @Override
        protected Source doInBackground(String... params) {
            if (params == null && params[0] == null) {
                return null;
            }
            try {
                URL url = new URL(params[0]);
                Logger.d(TAG, "request url : " + url.toString());
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
                Element contentElement = source.getFirstElementByClass("contents bbs_list");
                if (contentElement != null) {
                    SermonItem item = new SermonItem();
                    Logger.i(TAG, "contentElement : " + contentElement.toString());

                    //extract title & date
                    Element ttlElement = contentElement.getFirstElementByClass("bbs_ttl");
                    if (ttlElement != null) {
                        Logger.i(TAG, "onPostExecute > ttlElement : " + ttlElement.getTextExtractor().toString());
                        String temp = ttlElement.getTextExtractor().toString();
                        if (!TextUtils.isEmpty(temp) && temp.length() > 12) {
                            try {
                                String date = temp.substring(0, 12);
                                String title = temp.substring(13, temp.length());
                                item.title = title;
                                item.date = date;
                            } catch (Exception e) {
                                e.printStackTrace();
                                item.title = temp;
                            }
                        } else {
                            item.title = temp;
                        }
                    } else {
                        Logger.e(TAG, "onPostExecute > cannot find ttlElement element");
                    }

//                    item.title = getTitleFromElement(contentElement);
//                    item.date = getDateFromElement(contentElement);

                    //extract preacher and chapterInfo
                    Element temp = contentElement.getFirstElementByClass("bbs_substance_p");

                    for (Element element : temp.getAllElements(HTMLElementName.STRONG)) {
                        String tempStr = element.toString().replaceAll("&nbsp;", " ");
                        //Logger.d(TAG, "strong element : " + );
                        if (tempStr.contains("<br />")) {
                            String extractStr = element.getTextExtractor().toString();
                            String[] tempArr = null;
                            boolean isTitleSplit = false;
                            if (extractStr.contains("주제 : ")) {
                                tempArr = extractStr.split("주제 : ");
                                isTitleSplit = true;
                            } else if (extractStr.contains("본문 : ")) {
                                tempArr = extractStr.split("본문 : ");
                            }

                            if (tempArr != null && tempArr.length > 0) {
                                item.preacher = tempArr[0];
                                item.chapterInfo = (isTitleSplit ? "주제 : " : "본문 : ") + tempArr[1];
                            } else {
                                item.preacher = element.getTextExtractor().toString();
                            }//
                        } else {
                            if (tempStr.contains("설교 : ") || tempStr.contains("강의 : ")) {
                                item.preacher = element.getTextExtractor().toString();
                            } else if (tempStr.contains("본문 : ") || tempStr.contains("주제 : ")) {
                                item.chapterInfo = element.getTextExtractor().toString();
                            }
                        }
                    }

                    //extract attached file
                    List<Element> fileElement = contentElement.getAllElementsByClass("attch_file");
                    for (Element elem : fileElement) {
                        //Logger.i(TAG, "file element : " + elem.toString());
                        String href = elem.getFirstElement(HTMLElementName.A).getAttributeValue("href");
                        if (elem.getFirstElement(HTMLElementName.A).getAttributeValue("href").contains(".mp3")) {
                            item.audioFilePath = href;
                        } else if (elem.getFirstElement(HTMLElementName.A).getAttributeValue("href").contains("hwp")) {
                            item.docFilePath = href;
                        }
                    }
                    Logger.i(TAG, "worshipItem : " + item.toString());

                    mAdapter.add(item);

                } else {
                    Logger.e(TAG, "contentElement is null");
                    Logger.i(TAG, "source : " + source.toString());
                }
            } else {
                Logger.e(TAG, "source is null");
            }
        }
    }

    private String getTitleFromElement(Element elem) {
        Element titleElement = elem.getFirstElementByClass("bbs_ttl");
        if (titleElement != null) {
            Logger.i(TAG, "getTitleFromElement() > " + titleElement.getTextExtractor().toString());
            return titleElement.getTextExtractor().toString();
        }
        Logger.e(TAG, "getTitleFromElement() > cannot find title element");
        return null;
    }

    private String getDateFromElement(Element elem) {
        Element dateElement = elem.getFirstElementByClass("bbs_date");
        if (dateElement != null) {
            Logger.i(TAG, "getDateFromElement() > " + dateElement.getTextExtractor().toString());
            return dateElement.getTextExtractor().toString();
        }
        Logger.e(TAG, "getDateFromElement() > cannot find date element");
        return null;
    }

    public class SermonItem {
        public String title;
        public String content;
        public String preacher;
        public String chapterInfo;
        public String date;
        public String audioFilePath;
        public String docFilePath;

        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public class WorshipListAdapter extends ArrayAdapter<SermonItem> {
        private Context mContext;

        public WorshipListAdapter(Context context) {
            super(context, 0);
            mContext = context;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.sermon_row, null, false);
                vh = new ViewHolder(getActivity(), convertView, mService);
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
    }

    public static class ViewHolder {
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
