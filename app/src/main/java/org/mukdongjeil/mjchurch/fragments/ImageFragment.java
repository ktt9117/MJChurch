package org.mukdongjeil.mjchurch.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.util.Logger;

/**
 * Created by gradler on 02/05/2017.
 */

public class ImageFragment extends Fragment {

    private String imgUrl;

    public ImageFragment() {
        // Required empty public constructor
    }

    public void setImgUrl(String imgUrl) {
        Logger.e("ImageUrlFragment", "setImgUrl : " + imgUrl);
        this.imgUrl = imgUrl;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_url, container, false);
        PhotoView photoView = (PhotoView) v.findViewById(R.id.photoView);
        Glide.with(this).load(imgUrl).crossFade().into(photoView);
        return v;
    }
}
