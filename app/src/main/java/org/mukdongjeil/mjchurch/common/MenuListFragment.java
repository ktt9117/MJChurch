package org.mukdongjeil.mjchurch.common;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;

public class MenuListFragment extends ListFragment {

	public static final String ARGUMENT_MENU = "menu";

	public static final int MENU_TYPE_INTRODUCE = 0;
	public static final int MENU_TYPE_WORSHIP = 1;

	private static final String[] introduceMenus = {"연혁", "찾아오시는 길", "예배시간안내", "동역자"};
	private static final String[] worshipMenus = {"주일예배", "3부예배", "수요예배"};

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();
		int menuType = args.getInt(ARGUMENT_MENU);

		SampleAdapter adapter = new SampleAdapter(getActivity());
		for (int i = 0; i < 5; i++) {
			if (menuType == MENU_TYPE_INTRODUCE) {
				adapter.add(new MenuItem(introduceMenus[i], android.R.drawable.ic_menu_search));
			} else if (menuType == MENU_TYPE_WORSHIP) {
				adapter.add(new MenuItem(worshipMenus[i], android.R.drawable.ic_menu_search));
			}
		}
		setListAdapter(adapter);
	}

	private class MenuItem {
		public String title;
		public int iconRes;
		public MenuItem(String title, int iconRes) {
			this.title = title;
			this.iconRes = iconRes;
		}
	}

	public class SampleAdapter extends ArrayAdapter<MenuItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).title);

			return convertView;
		}

	}
}
