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
import com.google.firebase.crash.FirebaseCrash;
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

//        String textstr = "옆에 있는 사람이  버럭버럭 화를내고 자꾸 짜증을 낸다 더위탓인가....? 갱년기가 왔을까...? 그런데도 나는 자꾸 피식피식 웃음이 난다... (괜찮아요 저한텐 주님이 있잖아요 주님이 날 보고계셔서 기쁘거든요 제 말 듣고 계시죠?) 아지트나 베일에 쌓인 비밀이란 은밀해서 더 달콤한것이 아닌가? 명상과는 다른 묵상이...그리고 그분과 대화함의 달콤함이 나를 설레이게 한다 길거리에서 차 안에서 카페에서 순간순간. 형식도 없고 질서도 없이 나누는 대화이지만 아주 밀착되어있는 기분이 난 몹시 좋다  옆에 있는 사람이  버럭버럭 화를내고 자꾸 짜증을 낸다 더위탓인가....? 갱년기가 왔을까...? 그런데도 나는 자꾸 피식피식 웃음이 난다... (괜찮아요 저한텐 주님이 있잖아요 주님이 날 보고계셔서 기쁘거든요 제 말 듣고 계시죠?) 아지트나 베일에 쌓인 비밀이란 은밀해서 더 달콤한것이 아닌가? 명상과는 다른 묵상이...그리고 그분과 대화함의 달콤함이 나를 설레이게 한다 길거리에서 차 안에서 카페에서 순간순간. 형식도 없고 질서도 없이 나누는 대화이지만 아주 밀착되어있는 기분이 난 몹시 좋다  옆에 있는 사람이  버럭버럭 화를내고 자꾸 짜증을 낸다더위탓인가....? 갱년기가 왔을까...?그런데도 나는 자꾸 피식피식 웃음이 난다...(괜찮아요 저한텐 주님이 있잖아요 주님이 날 보고계셔서 기쁘거든요 제 말 듣고 계시죠?)아지트나 베일에 쌓인 비밀이란 은밀해서 더 달콤한것이 아닌가?명상과는 다른 묵상이...그리고 그분과 대화함의 달콤함이 나를 설레이게 한다길거리에서 차 안에서 카페에서 순간순간.형식도 없고 질서도 없이 나누는 대화이지만 아주 밀착되어있는 기분이 난 몹시 좋다";
//        StringUtils.removeDuplicationSentence(textstr);

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