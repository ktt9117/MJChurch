package org.mukdongjeil.mjchurch;

import android.app.Dialog;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_view.CycleProgressDialog;
import org.mukdongjeil.mjchurch.common.ext_view.ExViewPager;
import org.mukdongjeil.mjchurch.common.ext_view.SlidingTabLayout;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.PreferenceUtil;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.introduce.IntroduceFragment;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;
import org.mukdongjeil.mjchurch.worship.WorshipFragment;

public class MainActivity extends SlidingFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SAVED_INSTANCE_STATE_POSITION = "position";

    private static final int HANDLE_MSG_SHOW_SLIDING_TAB_LAYOUT = 1;
    private static final int HANDLE_MSG_HIDE_SLIDING_TAB_LAYOUT = 2;

    private SlidingTabLayout mSlidingTabLayout;
    private ExViewPager mViewPager;

    private CycleProgressDialog mLoadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemHelpers.init(getApplicationContext());
        PreferenceUtil.init(getApplicationContext());

        int setPosition = 0;
        if (savedInstanceState != null) {
            setPosition = savedInstanceState.getInt(SAVED_INSTANCE_STATE_POSITION);
        }

        // init Universal Image Loader
        initializeImageLoader();

        // init SlidingMenu
        initializeSlidingMenu(setPosition);

        // set the Content View
        setContentView(R.layout.activity_main);

        // init ViewPager
        initializePager(setPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
        showContent();
    }

    public void showContent() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                getSlidingMenu().showContent();
            }
        }, 100);
    }

    public void showLoadingDialog() {
        mLoadingDialog = new CycleProgressDialog(MainActivity.this);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    public void hideLoadingDialog() {
        releaseDialog(mLoadingDialog);
    }

    /*
    private ExHandler<MainActivity> mHandler = new ExHandler<MainActivity>(this) {
        @Override
        protected void handleMessage(MainActivity reference, Message msg) {
            if (reference == null || reference.isFinishing()) {
                return;
            }

            switch(msg.what) {
                case HANDLE_MSG_HIDE_SLIDING_TAB_LAYOUT:
                    reference.mSlidingTabLayout.setVisibility(View.GONE);
                    break;
                case HANDLE_MSG_SHOW_SLIDING_TAB_LAYOUT:
                    reference.mSlidingTabLayout.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    */


    private void initializeImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                SystemHelpers.getApplicationContext())
                .denyCacheImageMultipleSizesInMemory()
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(SAVED_INSTANCE_STATE_POSITION, mViewPager.getCurrentItem());
    }

    private void initializeSlidingMenu(int setPosition) {
        // set the Behind View
        setBehindContentView(R.layout.menu_frame);

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setBehindScrollScale(0.0f);

        // set scale animation transformer
        sm.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {canvas.scale(percentOpen, 1, 0, 0);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSlidingActionBarEnabled(true);

        //set the sliding menu contents for first page
        changeSlidingMenuContents(setPosition);
    }

    private void initializePager(int setPosition) {
        ContentsPagerAdapter contentsPagerAdapter = new ContentsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ExViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(contentsPagerAdapter);
        mViewPager.setCurrentItem(setPosition);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setOnPageChangeListener(mOnPageChangeListener);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            Logger.d(TAG, "onPageSelected(" + position + ")");
            switch (position) {
            case 0:
                getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                break;
            default:
                getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                break;
            }

            changeSlidingMenuContents(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    };

    private void changeSlidingMenuContents(int currentPagePosition) {
        ListFragment fragment = new MenuListFragment();
        Bundle args = new Bundle();
        if (currentPagePosition == Const.INTRODUCE_PAGE_INDEX) {
            args.putInt(MenuListFragment.CURRENT_PAGER_INDEX, MenuListFragment.MENU_TYPE_INTRODUCE);
        } else if (currentPagePosition == Const.WORSHIP_PAGE_INDEX) {
            args.putInt(MenuListFragment.CURRENT_PAGER_INDEX, MenuListFragment.MENU_TYPE_WORSHIP);
        }
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.sliding_menu_container, fragment);
        transaction.commit();
    }

    private class ContentsPagerAdapter extends FragmentPagerAdapter {
        private final String TAG = ContentsPagerAdapter.class.getSimpleName();

        public ContentsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return Const.PAGER_MENUS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Const.PAGER_MENUS[position];
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case Const.WORSHIP_PAGE_INDEX:
                    return new WorshipFragment();
                case Const.INTRODUCE_PAGE_INDEX:
                default:
                    return new IntroduceFragment();
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            if (position == Const.INTRODUCE_PAGE_INDEX) {
                PreferenceUtil.setIntroduceLastSelectedMenuIndex(0);
            } else if (position == Const.WORSHIP_PAGE_INDEX) {
                PreferenceUtil.setWorshipLastSelectedMenuIndex(0);
            }
        }
    }

    private void releaseDialog(Dialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseDialog(mLoadingDialog);
    }
}