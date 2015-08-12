package org.mukdongjeil.mjchurch;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;

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
import org.mukdongjeil.mjchurch.common.MenuListFragment;
import org.mukdongjeil.mjchurch.common.adapter.MenuPagerAdapter;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.common.view.ExViewPager;
import org.mukdongjeil.mjchurch.common.view.SlidingTabLayout;

public class MainActivity extends SlidingFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int HANDLE_MSG_SHOW_SLIDING_TAB_LAYOUT = 1;
    private static final int HANDLE_MSG_HIDE_SLIDING_TAB_LAYOUT = 2;

    private SlidingTabLayout mSlidingTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SystemHelpers.init(getApplicationContext());

        // init Universal Image Loader
        initializeImageLoader();

        // init SlidingMenu
        initializeSlidingMenu();

        // set the Content View
        setContentView(R.layout.activity_main);

        // init ViewPager
        initializePager();
    }

    private void initializeImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(SystemHelpers.getApplicationContext())
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

    private void initializeSlidingMenu() {
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
            public void transformCanvas(Canvas canvas, float percentOpen) {
                canvas.scale(percentOpen, 1, 0, 0);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSlidingActionBarEnabled(true);

        //set the sliding menu contents for first page
        changeSlidingMenuContents(0);
    }

    private void initializePager() {
        MenuPagerAdapter menuPagerAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        ExViewPager menuPager = (ExViewPager) findViewById(R.id.pager);
        menuPager.setAdapter(menuPagerAdapter);
        menuPager.setCurrentItem(0);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(menuPager);
        mSlidingTabLayout.setOnPageChangeListener(mOnPageChangeListener);
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
            args.putInt(MenuListFragment.ARGUMENT_MENU, MenuListFragment.MENU_TYPE_INTRODUCE);
        } else if (currentPagePosition == Const.WORSHIP_PAGE_INDEX) {
            args.putInt(MenuListFragment.ARGUMENT_MENU, MenuListFragment.MENU_TYPE_WORSHIP);
        }
        fragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.sliding_menu_container, fragment);
        transaction.commit();
    }
}
