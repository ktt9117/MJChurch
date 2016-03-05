package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.htmlparser.jericho.Element;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_view.CirclePageIndicator;
import org.mukdongjeil.mjchurch.common.ext_view.ExViewPager;
import org.mukdongjeil.mjchurch.common.photoview.PhotoViewAttacher;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestImageListTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class BoardGalleryDetailFragment extends Fragment {
    private static final String TAG = BoardGalleryDetailFragment.class.getSimpleName();

    private static final RelativeLayout.LayoutParams MATCH_PARENT = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    private static final RelativeLayout.LayoutParams WRAP_CONTENT = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    private static final String LOADING_PHOTO_MESSAGE = "사진을 불러오는 중입니다";

    private static final String ARG_BOARD_TYPE = "boardType";
    private static final String ARG_CONTENT_NO = "contentNo";

    private int mBoardType;
    private String mContentNo;
    private ExViewPager mPager;
    private CirclePageIndicator mPagerIndicator;
    //private Button shareItemButton;

    public static BoardGalleryDetailFragment newInstance(int boardType, String contentNo) {
        BoardGalleryDetailFragment fragment = new BoardGalleryDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOARD_TYPE, boardType);
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
            mBoardType = getArguments().getInt(ARG_BOARD_TYPE);
            mContentNo = getArguments().getString(ARG_CONTENT_NO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_board_gallery_detail, container, false);
        mPager = (ExViewPager) v.findViewById(R.id.gallery_detail_pager);
        mPager.addOnPageChangeListener(mOnPageChangeListener);
        mPagerIndicator = (CirclePageIndicator) v.findViewById(R.id.pager_indicator);
        /*
        shareItemButton = (Button) v.findViewById(R.id.share_item_button);
        shareItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO : 사진 공유하기 버튼은 각 item 마다 두는것으로 수정 필요
                Logger.e(TAG, "getCurrentItem : " + mPager.getCurrentItem());

                View tempView = mPager.getChildAt(1);
                ImageView content = null;
                if (tempView instanceof ViewGroup) {
                    for (int i = 0; i < ((ViewGroup) tempView).getChildCount(); i++) {
                        if (((ViewGroup) tempView).getChildAt(i) instanceof ImageView) {
                            content = (ImageView) ((ViewGroup) tempView).getChildAt(i);
                            break;
                        }
                    }
                } else if (tempView instanceof ImageView){
                    content = (ImageView) tempView;
                } else {
                    Logger.e(TAG, "Warning! mPager.getChildAt View is not ViewGroup. It must be ViewGroup");
                    return;
                }

                if (content == null) {
                    Logger.e(TAG, "Warning! mPager.getChildAt View has no Image childView");
                    return;
                }

                Bitmap bitmap = ((BitmapDrawable)content.getDrawable()).getBitmap();
                if (bitmap == null) {
                    Logger.e(TAG, "Warning! bitmap is null");
                    return;
                }
                File cacheFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/tmp_share_image.jpg");
                boolean isFileSaved = false;
                try {
                    cacheFile.createNewFile();
                    FileOutputStream fosStream = new FileOutputStream(cacheFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fosStream);
                    fosStream.close();
                    isFileSaved = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                content.setDrawingCacheEnabled(false);

                if (isFileSaved) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheFile));
                    startActivity(Intent.createChooser(share, "사진 공유"));
                }
            }
        });
        */

        String requestUrl = (mBoardType == BoardFragment.BOARD_TYPE_GALLERY) ? Const.getGalleryContentUrl(mContentNo) : Const.getNewPersonContentUrl(mContentNo);
        new RequestImageListTask(requestUrl, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                if (obj != null && obj instanceof List && ((List) obj).size() > 0) {
                    DetailPagerAdapter adapter = new DetailPagerAdapter(getActivity(), (List<Element>) obj);
                    mPager.setAdapter(adapter);
                    mPagerIndicator.setViewPager(mPager);
                } else {
                    Toast.makeText(getActivity(), "사진을 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            }
        });

        ((MainActivity) getActivity()).showCloseMenuItem();
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
        private List<Element> itemList;
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
            imgLink = imgLink.replaceAll("&amp;", "&");
            if (!imgLink.contains("http")) {
                imgLink = Const.BASE_URL + imgLink;
            }

            ImageLoader.getInstance().loadImage(imgLink, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.VISIBLE);
                        holder.txtView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.txtView.setVisibility(View.INVISIBLE);
                        holder.imgPhoto.setImageResource(Const.DEFAULT_IMG_RESOURCE);
                    }
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.txtView.setVisibility(View.INVISIBLE);
                        holder.imgPhoto.setImageBitmap(bitmap);
                        new PhotoViewAttacher(holder.imgPhoto);
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    if (holder != null) {
                        holder.progress.setVisibility(View.INVISIBLE);
                        holder.txtView.setVisibility(View.INVISIBLE);
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
                    if (v != null) {
                        container.removeView(v);
                        v = null;
                    }
                }
            });
        }
    }

    private static class ViewHolder {
        RelativeLayout layout;
        ImageView imgPhoto;
        TextView txtView;
        ProgressBar progress;

        public ViewHolder(Context context) {
            imgPhoto = new ImageView(context);
            imgPhoto.setLayoutParams(MATCH_PARENT);
            imgPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imgPhoto.setTag("ChildImgView");

            progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
            WRAP_CONTENT.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            progress.setLayoutParams(WRAP_CONTENT);

            txtView = new TextView(context, null, android.R.attr.textAppearanceLarge);
            txtView.setText(LOADING_PHOTO_MESSAGE);
            txtView.setTextColor(Color.WHITE);
            txtView.setGravity(Gravity.CENTER);
            txtView.setLayoutParams(MATCH_PARENT);

            layout = new RelativeLayout(context);
            layout.setBackgroundColor(Color.parseColor("#99000000"));
            layout.addView(imgPhoto);
            layout.addView(progress);
            layout.addView(txtView);
        }
    }
}