package org.mukdongjeil.mjchurch.board;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BoardGalleryDetailFragment extends Fragment {
    private static final String TAG = BoardGalleryDetailFragment.class.getSimpleName();

    private static final String LOADING_PHOTO_MESSAGE = "사진을 불러오는 중입니다";

    private static final String ARG_BOARD_TYPE = "boardType";
    private static final String ARG_CONTENT_NO = "contentNo";

    private int mBoardType;
    private String mContentNo;
    private ExViewPager mPager;
    private CirclePageIndicator mPagerIndicator;
    private ProgressBar mProgressBar;
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
        mProgressBar = (ProgressBar) v.findViewById(R.id.detail_page_progress);

        String requestUrl = (mBoardType == BoardFragment.BOARD_TYPE_GALLERY) ? Const.getGalleryContentUrl(mContentNo) : Const.getNewPersonContentUrl(mContentNo);
        new RequestImageListTask(requestUrl, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                hideProgressBar();
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

    private void hideProgressBar() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
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

            holder.progress.setVisibility(View.VISIBLE);
            holder.txtView.setVisibility(View.VISIBLE);

            Glide.with(getActivity())
                    .load(imgLink)
                    .placeholder(Const.DEFAULT_IMG_RESOURCE)
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            holder.progress.setVisibility(View.INVISIBLE);
                            holder.txtView.setVisibility(View.INVISIBLE);
                            holder.funcLayout.setVisibility(View.INVISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progress.setVisibility(View.INVISIBLE);
                            holder.txtView.setVisibility(View.INVISIBLE);
                            holder.funcLayout.setVisibility(View.VISIBLE);
                            new PhotoViewAttacher(holder.imgPhoto);

                            holder.btnShare.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

//                                Answers.getInstance().logContentView(new ContentViewEvent()
//                                        .putContentName("앨범")
//                                        .putContentType("이벤트")
//                                        .putContentId("사진 공유하기"));

                                    Bitmap bitmap = ((BitmapDrawable)holder.imgPhoto.getDrawable()).getBitmap();
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

                                    if (isFileSaved) {
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("image/*");
                                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cacheFile));
                                        startActivity(Intent.createChooser(share, "사진 공유"));
                                    }
                                }
                            });
                            holder.btnDownload.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

//                                Answers.getInstance().logContentView(new ContentViewEvent()
//                                        .putContentName("앨범")
//                                        .putContentType("이벤트")
//                                        .putContentId("사진 내려받기"));

                                    Bitmap bitmap = ((BitmapDrawable)holder.imgPhoto.getDrawable()).getBitmap();
                                    if (bitmap == null) {
                                        Logger.e(TAG, "Warning! bitmap is null");
                                        return;
                                    }
                                    File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/묵동제일앨범");
                                    if (!path.exists()) {
                                        path.mkdir();
                                    }

                                    String fileName = "/img_" + new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date());
                                    Logger.i(TAG, "fileSave name : " + fileName);
                                    File cacheFile = new File(path.toString() + fileName + ".jpg");
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

                                    if (isFileSaved) {
                                        MediaScannerConnection.scanFile(getContext(), new String[] {cacheFile.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String s, Uri uri) {
                                                Logger.i(TAG, "onScanCompleted s : " + s + ", uri : " + uri.toString());
                                            }
                                        });
                                        Toast.makeText(getContext(), "사진 저장됨", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            return false;
                        }
                    })
                    .into(holder.imgPhoto);


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
        LinearLayout funcLayout;
        ImageView imgPhoto;
        TextView txtView;
        ProgressBar progress;
        Button btnShare;
        Button btnDownload;

        public ViewHolder(Context context) {
            RelativeLayout.LayoutParams funcLayoutParams =
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            funcLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            funcLayout = new LinearLayout(context);
            funcLayout.setBackgroundColor(Color.BLACK);
            funcLayout.setId(R.id.viewpager_funclayout);
            funcLayout.setOrientation(LinearLayout.HORIZONTAL);
            funcLayout.setLayoutParams(funcLayoutParams);

            LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            btnLayoutParams.weight = 1;
            btnShare = new Button(context);
            btnShare.setTextColor(Color.WHITE);
            btnShare.setText("공유하기");
            btnShare.setLayoutParams(btnLayoutParams);
            btnDownload = new Button(context);
            btnDownload.setTextColor(Color.WHITE);
            btnDownload.setText("내려받기");
            btnDownload.setLayoutParams(btnLayoutParams);
            funcLayout.addView(btnShare);
            funcLayout.addView(btnDownload);

            RelativeLayout.LayoutParams imgPhotoParams =
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            imgPhotoParams.addRule(RelativeLayout.ABOVE, funcLayout.getId());
            imgPhoto = new ImageView(context);
            imgPhoto.setLayoutParams(imgPhotoParams);
            imgPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);

            RelativeLayout.LayoutParams progressParams =
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            progress = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
            progressParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            progress.setId(R.id.viewpager_progress);
            progress.setLayoutParams(progressParams);

            RelativeLayout.LayoutParams txtViewParams =
                    new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            txtView = new TextView(context, null, android.R.attr.textAppearanceLarge);
            txtViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            txtViewParams.addRule(RelativeLayout.BELOW, progress.getId());
            txtView.setText(LOADING_PHOTO_MESSAGE);
            txtView.setTextColor(Color.WHITE);
            txtView.setLayoutParams(txtViewParams);

            layout = new RelativeLayout(context);
            layout.setBackgroundColor(Color.parseColor("#99000000"));
            layout.addView(funcLayout);
            layout.addView(imgPhoto);
            layout.addView(progress);
            layout.addView(txtView);
        }
    }
}