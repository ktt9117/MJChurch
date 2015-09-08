package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.htmlparser.jericho.Element;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_view.ExViewPager;
import org.mukdongjeil.mjchurch.common.photoview.PhotoViewAttacher;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestImageListTask;

import java.util.List;

public class BoardGalleryDetailFragment extends Fragment {
    private static final String TAG = BoardGalleryDetailFragment.class.getSimpleName();

    private static final RelativeLayout.LayoutParams MATCH_PARENT = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    private static final RelativeLayout.LayoutParams WRAP_CONTENT = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    private static final String ARG_CONTENT_NO = "contentNo";

    private String mContentNo;
    private ExViewPager mPager;


    public static BoardGalleryDetailFragment newInstance(String contentNo) {
        BoardGalleryDetailFragment fragment = new BoardGalleryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT_NO, contentNo);
        fragment.setArguments(args);
        return fragment;
    }

    public BoardGalleryDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mContentNo = getArguments().getString(ARG_CONTENT_NO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_board_gallery_detail, container, false);
        mPager = (ExViewPager) v.findViewById(R.id.gallery_detail_pager);
        mPager.setOnPageChangeListener(mOnPageChangeListener);
        String requestUrl = Const.getGalleryDetailURL(mContentNo);
        new RequestImageListTask(requestUrl, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj) {
                if (obj != null && obj instanceof List) {
                    DetailPagerAdapter adapter = new DetailPagerAdapter(getActivity(), (List<Element>) obj);
                    mPager.setAdapter(adapter);
                }
            }
        });

        return v;
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override
        public void onPageSelected(int position) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSlidingTouchMode((position == 0) ? SlidingMenu.TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_MARGIN);
            }
        }
        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private class DetailPagerAdapter extends PagerAdapter {

        private final String TAG = DetailPagerAdapter.class.getSimpleName();

        List<Element> itemList;

        private Context context;

        public DetailPagerAdapter(Context context, List<Element> itemList) {
            this.context = context;
            this.itemList = itemList;
        }

        @Override
        public int getCount() {
            return itemList != null ? itemList.size() : 0;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Logger.v(TAG, "instantiateItem called");

            final ViewHolder holder = new ViewHolder(context);
            Element media = itemList.get(position);

            String imgLink = media.getAttributeValue("src");
            Logger.d(TAG, "imgLink : " + (Const.BASE_URL + imgLink));
            ImageLoader.getInstance().loadImage(Const.BASE_URL + imgLink, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    if (holder != null) holder.progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.GONE);
                        holder.imgPhoto.setImageResource(Const.DEFAULT_IMG_RESOURCE);
                    }
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.GONE);
                        holder.imgPhoto.setImageBitmap(bitmap);
                        PhotoViewAttacher attacher = new PhotoViewAttacher(holder.imgPhoto);
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.GONE);
                        holder.imgPhoto.setImageResource(Const.DEFAULT_IMG_RESOURCE);
                    }
                }
            });

            View v = holder.layout;
            container.addView(v, 0);
            return v;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public boolean isViewFromObject(View pager, Object obj) {
            return pager == obj;
        }

        @Override
        public void destroyItem(final ViewGroup container, int position, final Object object) {
            new Handler().post(new Runnable() {
                public void run() {
                    View v = (View) object;
                    container.removeView(v);
                    v = null;
                }
            });
        }

        private class ViewHolder {
            RelativeLayout layout;
            ImageView imgPhoto;
            ProgressBar progress;

            public ViewHolder(Context context) {
                imgPhoto = new ImageView(context);
                imgPhoto.setLayoutParams(MATCH_PARENT);
                imgPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);

                progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
                progress.setIndeterminate(true);
                progress.setIndeterminateDrawable(context.getResources().getDrawable(R.mipmap.ic_progressring));
                progress.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                WRAP_CONTENT.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                progress.setLayoutParams(WRAP_CONTENT);

                layout = new RelativeLayout(context);
                layout.setBackgroundColor(Color.parseColor("#AB000000"));
                layout.addView(imgPhoto);
                layout.addView(progress);
            }
        }
    }
}
