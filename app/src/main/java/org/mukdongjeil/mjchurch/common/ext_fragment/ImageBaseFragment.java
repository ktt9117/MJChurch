package org.mukdongjeil.mjchurch.common.ext_fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by Kim SungJoong on 2015-08-12.
 */
public class ImageBaseFragment extends Fragment{

    private PhotoView photoView;
    public PhotoView getImageView() {
        return photoView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        childParams.gravity = Gravity.TOP| Gravity.CENTER_HORIZONTAL;
        photoView = new PhotoView(getActivity());
        photoView.setLayoutParams(childParams);

        scrollView.addView(photoView);

        return scrollView;
    }
}
