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
import org.mukdongjeil.mjchurch.board.BoardFragment;
import org.mukdongjeil.mjchurch.board.BoardGalleryFragment;
import org.mukdongjeil.mjchurch.common.dao.MenuItem;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.introduce.IntroduceFragment;
import org.mukdongjeil.mjchurch.sermon.SermonFragment;
import org.mukdongjeil.mjchurch.training.TrainingFragment;

public class MenuListFragment extends ListFragment {
	public static final String SELECTED_MENU_INDEX = "selected_menu_index";

	private static final String TAG = MenuListFragment.class.getSimpleName();

	//list position 0, 6, 10, 15
	private static final String[] groups = {"환영합니다", "예배와 말씀", "양육과 훈련", "게시판"};
	private static final int[] groups_ic = {R.mipmap.ic_menu_welcome, R.mipmap.ic_menu_worship, R.mipmap.ic_menu_training, R.mipmap.ic_menu_board};
	//list position 1 ~ 5
	private static final String[] introduceMenus = {"교회소개", "교회연혁", "찾아오는 길", "예배시간안내", "섬김의 동역자"};
	//list position 7 ~ 9
	private static final String[] worshipMenus = {"주일 오전 예배", "주일 오후 예배", "수요예배"};
	//list position 11 ~ 15
	private static final String[] trainingMenus = {"성경 공부", "양육반", "마더와이즈", "1:1 제자훈련"};
	//list position 16 ~ 18
	private static final String[] boardMenus = {"감사 나눔", "교회앨범", "새신자앨범"};

	private MenuListAdapter mAdapter;
	private int mLastSelectedMenuIndex;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Logger.d(TAG, "onActivityCreated");

		mLastSelectedMenuIndex = 1;
//		mIntroduceLastSelectedMenuIndex = PreferenceUtil.getIntroduceLastSelectedMenuIndex();
//		mWorshipLastSelectedMenuIndex = PreferenceUtil.getWorshipLastSelectedMenuIndex();

		initializeMenuList();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		MenuItem item = mAdapter.getItem(position);

		// return if group title clicked
		if (item == null || item.menuType == MenuItem.MENU_TYPE_GROUP) {
			Logger.i(TAG, "This is not clickable item or The item is null");
			return;
		}

		// return if previous select position clicked
		if (mLastSelectedMenuIndex == position) {
			hideSlide();
			return;
		}

		// set previous select position
		mLastSelectedMenuIndex = position;

		// notify to adapter for select position has been changed
		mAdapter.notifySelectedItemChanged(position);

		// makes content fragment
		Fragment newFragment;
		switch (item.menuCategory) {
			case MenuItem.MENU_CATEGORY_WORSHIP:
				newFragment = new SermonFragment();
				break;
			case MenuItem.MENU_CATEGORY_TRAINING:
				newFragment = new TrainingFragment();
				break;
			case MenuItem.MENU_CATEGORY_BOARD:
				if (position == BoardFragment.BOARD_TYPE_THANKS_SHARING) {
					newFragment = new BoardFragment();
				} else {
					newFragment = new BoardGalleryFragment();
				}
				break;
			case MenuItem.MENU_CATEGORY_INTRODUCE:
			default:
				newFragment = new IntroduceFragment();
				break;
		}

		// notify to main fragment container for replace content
		if (newFragment != null) {
			Bundle args = new Bundle();
			args.putInt(SELECTED_MENU_INDEX, position);
			newFragment.setArguments(args);
			switchContent(newFragment);
		}
	}

	private void initializeMenuList() {
		mAdapter = new MenuListAdapter(getActivity());
		for (int i = 0; i < groups.length; i++) {
			mAdapter.add(new MenuItem(groups[i]));
			String[] tempArr;
			int category;
			switch(i) {
				case 1:
					tempArr = worshipMenus;
					category = MenuItem.MENU_CATEGORY_WORSHIP;
					break;
				case 2:
					tempArr = trainingMenus;
					category = MenuItem.MENU_CATEGORY_TRAINING;
					break;
				case 3:
					tempArr = boardMenus;
					category = MenuItem.MENU_CATEGORY_BOARD;
					break;
				case 0:
				default:
					tempArr = introduceMenus;
					category = MenuItem.MENU_CATEGORY_INTRODUCE;
					break;
			}

			for (int j = 0; j < tempArr.length; j++) {
				mAdapter.add(new MenuItem(tempArr[j], groups_ic[i], category));
			}
		}

		setListAdapter(mAdapter);
	}

	private void switchContent(Fragment newFragment) {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).switchContent(newFragment);
		}
	}

	private void hideSlide() {
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).hideSlideMenu();
		}
	}
}
