package org.mukdongjeil.mjchurch.services;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;

import org.mukdongjeil.mjchurch.ext_components.CycleProgressDialog;

/**
 * Created by gradler on 27/07/2017.
 */

public class BaseActivity extends AppCompatActivity {

    private CycleProgressDialog mLoadingDialog;

    public void showLoadingDialog() {
        mLoadingDialog = new CycleProgressDialog(this);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    public void hideLoadingDialog() {
        releaseDialog(mLoadingDialog);
    }

    public boolean isLoadingDialogShowing() {
        return (mLoadingDialog != null && mLoadingDialog.isShowing());
    }

    private void releaseDialog(Dialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
