package org.mukdongjeil.mjchurch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_view.CycleProgressDialog;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.fragments.BoardPagerFragment;
import org.mukdongjeil.mjchurch.fragments.ImagePagerFragment;
import org.mukdongjeil.mjchurch.fragments.SermonPagerFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public interface PermissionCheckResultListener {
        void onResult(boolean isGranted);
    }

    private static final String TAG = MainActivity.class.getSimpleName();

    private CycleProgressDialog mLoadingDialog;

    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int REQUEST_CODE_PERMISSION_CHECK = 100;
    private PermissionCheckResultListener mPermissionResultListener;

    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for fcm notification click action.
        showPushMessageIfNecessary(getIntent());

        startActivity(new Intent(this, IntroActivity.class));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // set the Content View
        setContentView(R.layout.activity_main);

        // set SideMenu
        initializeSlidingMenu();

        // set the current Fragment
        Fragment fragment = new ImagePagerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Const.INTENT_KEY_PAGE_TYPE, Const.PAGE_TYPE_INTRODUCE);
        bundle.putStringArray(Const.INTENT_KEY_PAGE_TITLES, Const.INTRODUCE_MENU_NAMES);
        bundle.putStringArray(Const.INTENT_KEY_PAGE_URLS, Const.INTRODUCE_MENU_URLS);
        fragment.setArguments(bundle);
        switchContent(fragment);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        Fragment newFragment = null;

        int id = item.getItemId();

        if (id == R.id.nav_welcome) {
            newFragment = new ImagePagerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Const.INTENT_KEY_PAGE_TYPE, Const.PAGE_TYPE_INTRODUCE);
            bundle.putStringArray(Const.INTENT_KEY_PAGE_TITLES, Const.INTRODUCE_MENU_NAMES);
            bundle.putStringArray(Const.INTENT_KEY_PAGE_URLS, Const.INTRODUCE_MENU_URLS);
            newFragment.setArguments(bundle);

        } else if (id == R.id.nav_worship) {
            newFragment = new SermonPagerFragment();

        } else if (id == R.id.nav_training) {
            newFragment = new ImagePagerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Const.INTENT_KEY_PAGE_TYPE, Const.PAGE_TYPE_TRAINING);
            bundle.putStringArray(Const.INTENT_KEY_PAGE_TITLES, Const.TRAINING_MENU_NAMES);
            bundle.putStringArray(Const.INTENT_KEY_PAGE_URLS, Const.TRAINING_MENU_URLS);
            newFragment.setArguments(bundle);

        } else if (id == R.id.nav_board) {
            newFragment = new BoardPagerFragment();
        }

        // notify to main fragment container for replace content
        if (newFragment == null) {
            return false;
        }

        switchContent(newFragment);
        hideSlideMenu();
        return true;
    }

    public void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void switchContentWithBackStack(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void hideSlideMenu() {
        if (mDrawerLayout == null) { return; }
        mDrawerLayout.closeDrawers();
    }

    public void addContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).addToBackStack("detail").commit();
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

    @Override
    public void onBackPressed() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            hideLoadingDialog();
            return;
        }

        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showPushMessageIfNecessary(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Logger.e(TAG, "bundle is null");
            return;
        }

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
    }

    private void initializeSlidingMenu() {
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create Navigation drawer and inlfate layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void releaseDialog(Dialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
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

    public void startPermissionCheck(final String checkPermission, PermissionCheckResultListener listener) {
        if (listener == null) {
            Logger.e(TAG, "cannot check permission caused by listener is not valid");
            return;
        }

        if (TextUtils.isEmpty(checkPermission)) {
            Logger.e(TAG, "cannot check permission caused by permission parameter");
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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