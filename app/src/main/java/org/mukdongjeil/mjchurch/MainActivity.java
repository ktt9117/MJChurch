package org.mukdongjeil.mjchurch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.mukdongjeil.mjchurch.common.ext_view.CycleProgressDialog;
import org.mukdongjeil.mjchurch.introduce.IntroduceFragment;

public class MainActivity extends SlidingFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CycleProgressDialog mLoadingDialog;
    private boolean isTouchModeFullScreen = true;
    private boolean mNeedShowCloseMenuItem = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, IntroActivity.class));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // get the GCM Token > no needs to GCM Token getting bcuz using change for FCM
        //getInstanceIdToken();

        // init SlidingMenu
        initializeSlidingMenu();

        // set the Content View
        setContentView(R.layout.activity_main);

        // set the current Fragment
        Fragment fragment = new IntroduceFragment();
        switchContent(fragment);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                toggleTouchMode();
            }
        });
    }

    public void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        showContent();
    }

    public void hideSlideMenu() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                getSlidingMenu().showContent();
            }
        }, 100);
    }

    public void addContent(Fragment fragment) {
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).addToBackStack("detail").commit();
        showContent();
    }

    public void showLoadingDialog() {
        mLoadingDialog = new CycleProgressDialog(MainActivity.this);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.show();
    }

    public void hideLoadingDialog() {
        releaseDialog(mLoadingDialog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseDialog(mLoadingDialog);
    }

    public void setSlidingTouchMode(int touchMode) {
        if (getSlidingMenu() != null) {
            getSlidingMenu().setTouchModeAbove(touchMode);
        }
    }

    private void toggleTouchMode() {
        if (getSlidingMenu() != null) {
            if (isTouchModeFullScreen) {
                isTouchModeFullScreen = false;
                getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            } else {
                isTouchModeFullScreen = true;
                getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mNeedShowCloseMenuItem) {
            getMenuInflater().inflate(R.menu.menu_close, menu);
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getSlidingMenu().toggle(true);
                return true;
            case R.id.action_mode_close_button:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mNeedShowCloseMenuItem) {
            mNeedShowCloseMenuItem = false;
            invalidateOptionsMenu();
        }
    }

    private void initializeSlidingMenu() {
        // set the Behind View
        setBehindContentView(R.layout.fragment_slide_menu);

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        sm.setBehindScrollScale(0.0f);

        // set scale animation transformer
        sm.setBehindCanvasTransformer(new SlidingMenu.CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                canvas.scale(percentOpen, 1, 0, 0);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setSlidingActionBarEnabled(true);
    }

    private void releaseDialog(Dialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = null;
        }
    }

    public interface NetworkAlertResultListener {
        void onClick(boolean positiveButtonClick);
    }

    public void showNetworkAlertDialog(String message, final NetworkAlertResultListener listener) {
        AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
        ab.setTitle("경고");
        ab.setCancelable(false);
        ab.setMessage(message);
        ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(true);
            }
        });
        ab.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(false);
            }
        });
        ab.create().show();
    }

    public void showCloseMenuItem() {
        mNeedShowCloseMenuItem = true;
        invalidateOptionsMenu();
    }
}