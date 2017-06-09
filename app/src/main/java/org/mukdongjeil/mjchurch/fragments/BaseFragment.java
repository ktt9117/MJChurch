package org.mukdongjeil.mjchurch.fragments;

import android.support.v4.app.Fragment;

import org.mukdongjeil.mjchurch.MainActivity;

/**
 * Created by gradler on 11/05/2017.
 */

public class BaseFragment extends Fragment {

    protected void showLoadingDialog() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }
    }

    protected void closeLoadingDialog() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideLoadingDialog();
        }
    }

}
