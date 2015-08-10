package org.mukdongjeil.mjchurch.worship;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.R;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class WorshipFragment extends Fragment {
    public static final String TAG = "Worship";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_introduce, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.textView)).setText(Integer.toString(args.getInt(TAG)));
        return rootView;
    }
}
