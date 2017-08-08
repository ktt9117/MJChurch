package org.mukdongjeil.mjchurch.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.Const;

import java.util.ArrayList;
import java.util.List;

public class BoardPagerFragment extends BaseFragment {
    private static final String TAG = BoardPagerFragment.class.getSimpleName();

    private ViewPager mPager;
    private TabLayout mTabs;

    // Board menu names
    private static final String[] BOARD_TAP_NAMES = {"감사 나눔", "교회앨범", "새신자앨범"};

    public BoardPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pager, container, false);
        mPager = (ViewPager) v.findViewById(R.id.viewpager);
        mTabs = (TabLayout) v.findViewById(R.id.tabs);
        mTabs.setTabMode(TabLayout.MODE_FIXED);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.menu_board);
        setupPagerAdapter();
    }

    private void setupPagerAdapter() {
        BoardPagerAdapter adapter = new BoardPagerAdapter(getChildFragmentManager());

        BoardFragment boardFragment = new BoardFragment();
        adapter.addFragment(boardFragment, BOARD_TAP_NAMES[0]);

        BoardGalleryFragment albumFragment = new BoardGalleryFragment();
        Bundle albumArgs = new Bundle();
        albumArgs.putInt(Const.INTENT_KEY_SELECTED_MENU_INDEX, 18);
        albumFragment.setArguments(albumArgs);
        adapter.addFragment(albumFragment, BOARD_TAP_NAMES[1]);

        BoardGalleryFragment newPeopleFragment = new BoardGalleryFragment();
        Bundle newPeopleArgs = new Bundle();
        newPeopleArgs.putInt(Const.INTENT_KEY_SELECTED_MENU_INDEX, 19);
        newPeopleFragment.setArguments(newPeopleArgs);
        adapter.addFragment(newPeopleFragment, BOARD_TAP_NAMES[2]);

        mTabs.setupWithViewPager(mPager);
        mPager.setAdapter(adapter);
    }

    private static class BoardPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        protected BoardPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragmentTitleList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        protected void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}