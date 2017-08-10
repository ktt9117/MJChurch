package org.mukdongjeil.mjchurch.fragments;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.Logger;

/**
 * Created by gradler on 10/08/2017.
 */

public class LoadingMenuBaseFragment extends BaseFragment {
    private static final String TAG = LoadingMenuBaseFragment.class.getSimpleName();

    private static final int MSG_WHAT_HIDE_ACTIONBAR_PROGRESS = 9117;
    private boolean mIsLoadingItems = false;

    private ExHandler<LoadingMenuBaseFragment> mHandler = new ExHandler<LoadingMenuBaseFragment>(this) {
        @Override
        protected void handleMessage(LoadingMenuBaseFragment reference, Message msg) {
            if (reference == null) {
                Logger.e(TAG, "error occured on handleMessage caused by reference param is null.");
                return;
            }

            if (msg.what == MSG_WHAT_HIDE_ACTIONBAR_PROGRESS) {
                reference.hideActionBarProgress();
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_progress, menu);
        MenuItem item = menu.getItem(0);
        if (item != null) {
            ProgressBar progressView = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyle);
            progressView.setPadding(0, 8, 32, 8);
            item.setActionView(mIsLoadingItems ? progressView : null);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        mHandler.removeMessages(MSG_WHAT_HIDE_ACTIONBAR_PROGRESS);
        super.onPause();
    }

    protected void showActionBarProgress() {
        mIsLoadingItems = true;
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    protected void hideActionBarProgress() {
        mIsLoadingItems = false;
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    protected void hideActionBarProgressDelayed(long timeInMillis) {
        mHandler.removeMessages(MSG_WHAT_HIDE_ACTIONBAR_PROGRESS);
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_HIDE_ACTIONBAR_PROGRESS, timeInMillis);
    }
}