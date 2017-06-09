package org.mukdongjeil.mjchurch.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import net.htmlparser.jericho.Element;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_view.CirclePageIndicator;
import org.mukdongjeil.mjchurch.common.ext_view.ExViewPager;
import org.mukdongjeil.mjchurch.common.util.ImageUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.models.GalleryDetail;
import org.mukdongjeil.mjchurch.models.RealmString;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestImageListTask;
import org.mukdongjeil.mjchurch.service.DataService;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

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
    private ImageView mBtnClose;
    private DetailPagerAdapter mAdapter;
    private Realm realm;

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
        realm = Realm.getDefaultInstance();
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mBoardType = getArguments().getInt(ARG_BOARD_TYPE);
            mContentNo = getArguments().getString(ARG_CONTENT_NO);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_board_gallery_detail, container, false);
        mPager = (ExViewPager) v.findViewById(R.id.gallery_detail_pager);
        mPagerIndicator = (CirclePageIndicator) v.findViewById(R.id.pager_indicator);
        mProgressBar = (ProgressBar) v.findViewById(R.id.detail_page_progress);
        mBtnClose = (ImageView) v.findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        String requestUrl = (mBoardType == BoardFragment.BOARD_TYPE_GALLERY) ? Const.getGalleryContentUrl(mContentNo) : Const.getNewPersonContentUrl(mContentNo);
        GalleryDetail item = DataService.getGalleryDetail(realm, mContentNo);
        if (item != null && item.imageUrlList != null && item.imageUrlList.size() > 0) {
            hideProgressBar();
            mAdapter = new DetailPagerAdapter(getActivity(), item.imageUrlList);
            mPager.setAdapter(mAdapter);
            mPagerIndicator.setViewPager(mPager);
        } else {
            new RequestImageListTask(requestUrl, new RequestBaseTask.OnResultListener() {
                @Override
                public void onResult(Object obj, int position) {
                    hideProgressBar();
                    List<Element> elementList = (List<Element>) obj;
                    RealmList<RealmString> itemList = new RealmList<>();
                    if (elementList != null && elementList.size() > 0) {
                        for (Element element : elementList) {
                            String imgLink = element.getAttributeValue("src");
                            Logger.d(TAG, "imgLink : " + (Const.BASE_URL + imgLink));
                            imgLink = imgLink.replaceAll("&amp;", "&");
                            if (!imgLink.contains("http")) {
                                imgLink = Const.BASE_URL + imgLink;
                            }

                            itemList.add(new RealmString(imgLink));
                        }

                        GalleryDetail detail = new GalleryDetail();
                        detail.contentNo = mContentNo;
                        detail.imageUrlList = itemList;
                        DataService.insertToRealm(realm, detail);

                        mAdapter = new DetailPagerAdapter(getActivity(), itemList);
                        mPager.setAdapter(mAdapter);
                        mPagerIndicator.setViewPager(mPager);
                    } else {
                        Toast.makeText(getActivity(), "사진을 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        return v;
    }

    private void hideProgressBar() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gallery_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View v;
        switch (item.getItemId()) {
        case R.id.action_btn_share:
            Logger.i(TAG, "actionBtnShare");
            v = mAdapter.currentView;
            if (v != null) {
                ViewHolder vh = (ViewHolder) v.getTag();
                if (vh != null) {
                    mAdapter.shareImage(vh.photoView);
                }
            }
            return true;
        case R.id.action_btn_download:
            Logger.i(TAG, "actionBtnDownload");
            v = mAdapter.currentView;
            if (v != null) {
                ViewHolder vh = (ViewHolder) v.getTag();
                if (vh != null) {
                    mAdapter.saveImage(vh.photoView);
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DetailPagerAdapter extends PagerAdapter {
        private RealmList<RealmString> itemList;
        private Context context;
        private View currentView;

        public DetailPagerAdapter(Context context, RealmList<RealmString> itemList) {
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

            holder.progress.setVisibility(View.VISIBLE);
            holder.txtView.setVisibility(View.VISIBLE);

            Glide.with(getActivity())
                    .load(itemList.get(position).getValue())
                    .placeholder(Const.DEFAULT_IMG_RESOURCE)
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            holder.progress.setVisibility(View.INVISIBLE);
                            holder.txtView.setVisibility(View.INVISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progress.setVisibility(View.INVISIBLE);
                            holder.txtView.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(holder.photoView);


            View v = holder.layout;
            v.setTag(holder);
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
                    }
                }
            });
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentView = (View) object;
            super.setPrimaryItem(container, position, object);
        }

        private void shareImage(PhotoView photoView) {
            Bitmap bitmap = ImageUtil.convertDrawableToBitmap(photoView.getDrawable());
            if (bitmap == null) {
                Logger.e(TAG, "Warning! bitmap is null");
                Toast.makeText(getActivity(), "이미지를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
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

        private void saveImage(PhotoView photoView) {

            Bitmap bitmap = ImageUtil.convertDrawableToBitmap(photoView.getDrawable());
            if (bitmap == null) {
                Logger.e(TAG, "Warning! bitmap is null");
                Toast.makeText(getActivity(), "이미지를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
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
                MediaScannerConnection.scanFile(getContext(), new String[]{cacheFile.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Logger.i(TAG, "onScanCompleted s : " + s + ", uri : " + uri.toString());
                    }
                });
                Toast.makeText(getContext(), "사진 저장됨", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class ViewHolder {
        RelativeLayout layout;
        PhotoView photoView;
        TextView txtView;
        ProgressBar progress;

        public ViewHolder(Context context) {
            RelativeLayout.LayoutParams imgPhotoParams =
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            photoView = new PhotoView(context);
            photoView.setLayoutParams(imgPhotoParams);
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            RelativeLayout.LayoutParams progressParams =
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            progress = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
            progressParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            progress.setId(R.id.viewpager_progress);
            progress.setLayoutParams(progressParams);

            RelativeLayout.LayoutParams txtViewParams =
                    new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            txtView = new TextView(context, null, android.R.attr.textAppearanceMedium);
            txtViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            txtViewParams.addRule(RelativeLayout.BELOW, progress.getId());
            txtView.setText(LOADING_PHOTO_MESSAGE);
            txtView.setTextColor(Color.WHITE);
            txtView.setLayoutParams(txtViewParams);

            layout = new RelativeLayout(context);
            layout.setBackgroundColor(Color.parseColor("#99000000"));
            layout.addView(photoView);
            layout.addView(progress);
            layout.addView(txtView);
        }
    }
}