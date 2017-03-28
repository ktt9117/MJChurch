package org.mukdongjeil.mjchurch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import org.mukdongjeil.mjchurch.common.ext_view.CycleProgressDialog;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.introduce.IntroduceFragment;

public class MainActivity extends SlidingFragmentActivity {
    public interface PermissionCheckResultListener {
        void onResult(boolean isGranted);
    }

    private static final String TAG = MainActivity.class.getSimpleName();

    private CycleProgressDialog mLoadingDialog;
    private boolean isTouchModeFullScreen = true;
    private boolean mNeedShowCloseMenuItem = false;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int REQUEST_CODE_PERMISSION_CHECK = 100;
    private PermissionCheckResultListener mPermissionResultListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for fcm notification click action.
        Intent getIntent = getIntent();
        if (getIntent != null) {
            Bundle bundle = getIntent.getExtras();
            if (bundle != null) {
                final String message = bundle.getString("message");
                if (TextUtils.isEmpty(message) == false) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing() == false) {
                                Intent intent = new Intent(MainActivity.this, PushMessageActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("message", message);
                                startActivity(intent);
                            } else {
                                Logger.i(TAG, "isFinishing() : " + isFinishing());
                            }
                        }
                    }, 2300);
                }
            } else {
                Logger.e(TAG, "bundle is null");
            }
        }

        startActivity(new Intent(this, IntroActivity.class));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        showContent();
    }

    public void switchContentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        getActionBar().setDisplayHomeAsUpEnabled(false);
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
            Logger.e(TAG, "onPrepareOptionsMenu : needToCloseMenuItem");
            getMenuInflater().inflate(R.menu.menu_close, menu);
            return true;
        }
        Logger.e(TAG, "onPrepareOptionsMenu : there is no need to show menu items");
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

    public void startPermissionCheck(final String checkPermission, PermissionCheckResultListener listener) {
        if (TextUtils.isEmpty(checkPermission) || listener == null) {
            Logger.e(TAG, "cannot check permission caused by permission parameter or listener is not valid");
            listener.onResult(false);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, checkPermission) != PackageManager.PERMISSION_GRANTED) {

            mPermissionResultListener = listener;

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, checkPermission)) {
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                String rationale;
                if (checkPermission.equals(Manifest.permission.READ_PHONE_STATE)) {
                    rationale = "오디오 재생을 위해서는 \"통화 상태 조회\" 권한이 필요합니다. 계속 진행하려면 다음 화면에서 허용을 눌러주세요.";
                } else if (checkPermission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    rationale = "설교 다운로드를 위해서는 \"쓰기\" 권한이 필요합니다. 계속 진행하려면 다음 화면에서 허용을 눌러주세요.";
                } else {
                    Logger.e(TAG, "cannot check permission caused by checkPermission parameter is not valid");
                    mPermissionResultListener = null;
                    return;
                }
                ab.setTitle("권한이 필요합니다.");
                ab.setMessage(rationale);
                ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{checkPermission}, REQUEST_CODE_PERMISSION_CHECK);

                    }
                });
                ab.create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{checkPermission}, REQUEST_CODE_PERMISSION_CHECK);
            }
        } else {
            listener.onResult(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mPermissionResultListener == null) {
            Logger.e(TAG, "onRequestPermissionResult just pass. caused by resultListener is null");
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mPermissionResultListener.onResult(true);
        } else {
            mPermissionResultListener.onResult(false);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}