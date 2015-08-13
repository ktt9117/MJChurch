package org.mukdongjeil.mjchurch.common.ext_fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.ext_view.CycleProgressDialog;

/**
 * Created by Kim SungJoong on 2015-08-12.
 */
public class ImageBaseFragment extends Fragment{

    private ImageView mImageView;
    public ImageView getImageView() {
        return mImageView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.pager_base_fragment, container, false);
        ((ViewGroup) rootView).removeAllViews();
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        childParams.gravity = Gravity.TOP| Gravity.CENTER_HORIZONTAL;
        mImageView = new ImageView(getActivity());
        mImageView.setLayoutParams(childParams);
        scrollView.addView(mImageView);

        ((ViewGroup) rootView).addView(scrollView);
        return rootView;
    }
}
