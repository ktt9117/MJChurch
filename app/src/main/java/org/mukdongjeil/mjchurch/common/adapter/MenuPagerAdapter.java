package org.mukdongjeil.mjchurch.common.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.introduce.IntroduceFragment;
import org.mukdongjeil.mjchurch.worship.WorshipFragment;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class MenuPagerAdapter extends FragmentPagerAdapter {
    private static final String[] MENUS = new String[] {"Introduce", "Worship", "Training", "Groups", "Board"};

    public MenuPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return MENUS.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MENUS[position];
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new IntroduceFragment();
        if (i == Const.WORSHIP_PAGE) {
            fragment = new WorshipFragment();
        }

        return fragment;
    }
}