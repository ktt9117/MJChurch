package org.mukdongjeil.mjchurch.slidingmenu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.dao.MenuItem;

/**
 * Created by Kim SungJoong on 2015-08-13.
 */
public class MenuListAdapter extends ArrayAdapter<MenuItem> {

    private int selectedItemPosition = 0;

    public MenuListAdapter(Context context) {
        super(context, 0);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_menu, null);
        }

        MenuItem item = getItem(position);

        ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
        TextView title = (TextView) convertView.findViewById(R.id.row_title);

        if (item.menuType == MenuItem.MENU_TYPE_GROUP) {
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(item.iconRes);
            title.setTextAppearance(getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
            convertView.setEnabled(false);
            convertView.setBackgroundColor(Color.parseColor("#bebebe"));
        } else {
            icon.setVisibility(View.GONE);
            title.setTextAppearance(getContext(), android.R.style.TextAppearance_DeviceDefault_Small);
            convertView.setEnabled(true);
            if (selectedItemPosition == position) {
                convertView.setBackgroundColor(Color.LTGRAY);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        title.setText(item.title);
        return convertView;
    }

    public void notifySelectedItemChanged(int position) {
        selectedItemPosition = position;
        notifyDataSetChanged();
    }
}
