package org.mukdongjeil.mjchurch;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.mukdongjeil.mjchurch.common.MenuListFragment;
import org.mukdongjeil.mjchurch.common.adapter.MenuPagerAdapter;
import org.mukdongjeil.mjchurch.common.util.ExHandler;
import org.mukdongjeil.mjchurch.common.view.ExViewPager;
import org.mukdongjeil.mjchurch.common.view.SlidingTabLayout;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int HANDLE_MSG_SHOW_SLIDING_TAB_LAYOUT = 1;
    private static final int HANDLE_MSG_HIDE_SLIDING_TAB_LAYOUT = 2;

    private SlidingTabLayout mSlidingTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init SlidingMenu
        initializeSlidingMenu();

        // set the Content View
        setContentView(R.layout.activity_main);

        // init ViewPager
        initializePager();
    }

    private void initializeSlidingMenu() {
        // set the Behind View
        View view = getLayoutInflater().inflate(R.layout.introduce_menu, null);
        setBehindContentView(view);

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializePager() {
        MenuPagerAdapter menuPagerAdapter = new MenuPagerAdapter(getSupportFragmentManager());
        menuPagerAdapter.setParent(this);
        ExViewPager menuPager = (ExViewPager) findViewById(R.id.pager);
        menuPager.setAdapter(menuPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(menuPager);
        mSlidingTabLayout.setOnPageChangeListener(mOnPageChangeListener);
    }

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

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            Fragment fragment = new MenuListFragment();
            Bundle args = new Bundle();

            if (position == 0) {
                args.putInt(MenuListFragment.ARGUMENT_MENU, MenuListFragment.MENU_TYPE_INTRODUCE);
            } else if (position == 1) {
                args.putInt(MenuListFragment.ARGUMENT_MENU, MenuListFragment.MENU_TYPE_WORSHIP);
            }

            fragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.menu_list_fragment, fragment);
            transaction.commit();

        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    };
}
