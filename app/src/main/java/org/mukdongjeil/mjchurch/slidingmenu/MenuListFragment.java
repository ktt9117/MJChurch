package org.mukdongjeil.mjchurch.slidingmenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.dao.MenuItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.PreferenceUtil;
import org.mukdongjeil.mjchurch.introduce.IntroduceFragment;
import org.mukdongjeil.mjchurch.worship.WorshipFragment;

public class MenuListFragment extends ListFragment {

	public static final String CURRENT_PAGER_INDEX = "menu";
	public static final String SELECTED_MENU_INDEX = "selected_menu_index";

	public static final int MENU_TYPE_INTRODUCE = 0;
	public static final int MENU_TYPE_WORSHIP = 1;

	private static final String TAG = MenuListFragment.class.getSimpleName();

	private static final String[] introduceMenus = {"교회소개", "연혁", "찾아오시는 길", "예배시간안내", "동역자"};
	private static final String[] worshipMenus = {"주일예배", "3부예배", "수요예배"};

	private int mIntroduceLastSelectedMenuIndex = 0;
	private int mWorshipLastSelectedMenuIndex = 0;

	private MenuListAdapter mAdapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	private int mMenuType = -1;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Logger.d(TAG, "onActivityCreated");

		Logger.d(TAG, "savedInstance is null.");
		Bundle args = getArguments();
		if (args == null) {
			Logger.e(TAG, "there is no arguments. just return onActivityCreated()");
			return;
		}
		mMenuType = args.getInt(CURRENT_PAGER_INDEX);
		Logger.d(TAG, "mMenuType : " + mMenuType);

		mIntroduceLastSelectedMenuIndex = PreferenceUtil.getIntroduceLastSelectedMenuIndex();
		mWorshipLastSelectedMenuIndex = PreferenceUtil.getWorshipLastSelectedMenuIndex();

		initializeMenuList();
	}

	private void initializeMenuList() {
		mAdapter = new MenuListAdapter(getActivity());
		if (mMenuType == MENU_TYPE_INTRODUCE) {
			for (int i = 0; i < introduceMenus.length; i++) {
				mAdapter.add(new MenuItem(introduceMenus[i], android.R.drawable.ic_menu_search));
				mAdapter.selectedItemPositionChanged(mIntroduceLastSelectedMenuIndex);
			}
		} else if (mMenuType == MENU_TYPE_WORSHIP) {
			for (int i = 0; i < worshipMenus.length; i++) {
				mAdapter.add(new MenuItem(worshipMenus[i], android.R.drawable.ic_menu_search));
				mAdapter.selectedItemPositionChanged(mIntroduceLastSelectedMenuIndex);
			}
		}
		setListAdapter(mAdapter);
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Fragment newFragment;
		switch (mMenuType) {
			case MENU_TYPE_WORSHIP:
				if (mWorshipLastSelectedMenuIndex == position) {
					showContent();
					return;
				}

				if (mAdapter != null) {
					mAdapter.selectedItemPositionChanged(position);
				}
				PreferenceUtil.setWorshipLastSelectedMenuIndex(position);
				mWorshipLastSelectedMenuIndex = position;
				newFragment = new WorshipFragment();
				break;
			case MENU_TYPE_INTRODUCE:
			default:
				if (mIntroduceLastSelectedMenuIndex == position) {
					showContent();
					return;
				}

				if (mAdapter != null) {
					mAdapter.selectedItemPositionChanged(position);
				}
				PreferenceUtil.setIntroduceLastSelectedMenuIndex(position);
				mIntroduceLastSelectedMenuIndex = position;
				newFragment = new IntroduceFragment();
				break;
		}

		if (newFragment != null) {
			Bundle args = new Bundle();
			args.putInt(SELECTED_MENU_INDEX, position);
			newFragment.setArguments(args);
			switchContent(newFragment);
		}
	}

	private void switchContent(Fragment newFragment) {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).switchContent(newFragment);
		}
	}

	private void showContent() {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).showContent();
		}
	}
}
