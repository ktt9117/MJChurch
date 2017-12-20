package org.mukdongjeil.mjchurch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.mukdongjeil.mjchurch.activities.BaseActivity;
import org.mukdongjeil.mjchurch.fragments.BoardPagerFragment;
import org.mukdongjeil.mjchurch.fragments.ChatFragment;
import org.mukdongjeil.mjchurch.fragments.ImagePagerFragment;
import org.mukdongjeil.mjchurch.fragments.SermonFragment;
import org.mukdongjeil.mjchurch.utils.Logger;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PERMISSION_CHECK = 100;

    private PermissionCheckResultListener mPermissionResultListener;
    private DrawerLayout mDrawerLayout;
    private boolean mIsReallyExit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for fcm notification click action.
        showPushMessageIfNecessary(getIntent());

        // start intro activity
        if (Const.DEBUG_MODE == false) {
            startActivity(new Intent(this, IntroActivity.class));
            // get the firebase instance
            FirebaseAnalytics.getInstance(this);
        }

        // set the content view
        setContentView(R.layout.activity_main);

        // set the slide menu
        initializeSlidingMenu();

        // set the start fragment
        String action = getIntent().getAction();
        Logger.e(TAG, "intent action : " + action);

        if (!TextUtils.isEmpty(action) && action.equals(Const.INTENT_ACTION_OPEN_CHAT)) {
            Fragment fragment = new ChatFragment();
            switchContent(fragment);

        } else {
            Fragment fragment = new ImagePagerFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(Const.INTENT_KEY_PAGE_TYPE, Const.PAGE_TYPE_INTRODUCE);
            bundle.putStringArray(Const.INTENT_KEY_PAGE_TITLES, Const.INTRODUCE_MENU_NAMES);
            bundle.putStringArray(Const.INTENT_KEY_PAGE_URLS, Const.INTRODUCE_MENU_URLS);
            fragment.setArguments(bundle);
            switchContent(fragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
    }

    @Override
    public void onBackPressed() {
        if (isLoadingDialogShowing()) {
            hideLoadingDialog();
            return;
        }

        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);

        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return;
            }

            if (mIsReallyExit) {
                super.onBackPressed();

            } else {
                mIsReallyExit = true;
                Toast.makeText(getApplicationContext(), R.string.application_quit_message, Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsReallyExit = false;
                    }
                }, 2000);
            }
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment newFragment;
        Bundle bundle;

        switch (item.getItemId()) {
            default:
            case R.id.nav_welcome:
                newFragment = new ImagePagerFragment();
                bundle = new Bundle();
                bundle.putInt(Const.INTENT_KEY_PAGE_TYPE, Const.PAGE_TYPE_INTRODUCE);
                bundle.putStringArray(Const.INTENT_KEY_PAGE_TITLES, Const.INTRODUCE_MENU_NAMES);
                bundle.putStringArray(Const.INTENT_KEY_PAGE_URLS, Const.INTRODUCE_MENU_URLS);
                newFragment.setArguments(bundle);
                break;
            case R.id.nav_training:
                newFragment = new ImagePagerFragment();
                bundle = new Bundle();
                bundle.putInt(Const.INTENT_KEY_PAGE_TYPE, Const.PAGE_TYPE_TRAINING);
                bundle.putStringArray(Const.INTENT_KEY_PAGE_TITLES, Const.TRAINING_MENU_NAMES);
                bundle.putStringArray(Const.INTENT_KEY_PAGE_URLS, Const.TRAINING_MENU_URLS);
                newFragment.setArguments(bundle);
                break;
            case R.id.nav_board:
                newFragment = new BoardPagerFragment();
                break;
            case R.id.nav_chat:
                newFragment = new ChatFragment();
                break;
            case R.id.nav_worship_sunday_morning:
                newFragment = new SermonFragment();
                bundle = new Bundle();
                bundle.putString(Const.INTENT_KEY_TITLE, getString(R.string.worship_sunday_morning));
                bundle.putInt(Const.INTENT_KEY_WORSHIP_TYPE, Const.WORSHIP_TYPE_SUNDAY_MORNING);
                newFragment.setArguments(bundle);
                break;
            case R.id.nav_worship_sunday_afternoon:
                newFragment = new SermonFragment();
                bundle = new Bundle();
                bundle.putString(Const.INTENT_KEY_TITLE, getString(R.string.worship_sunday_afternoon));
                bundle.putInt(Const.INTENT_KEY_WORSHIP_TYPE, Const.WORSHIP_TYPE_SUNDAY_AFTERNOON);
                newFragment.setArguments(bundle);
                break;
            case R.id.nav_worship_wednesday:
                newFragment = new SermonFragment();
                bundle = new Bundle();
                bundle.putString(Const.INTENT_KEY_TITLE, getString(R.string.worship_wednesday));
                bundle.putInt(Const.INTENT_KEY_WORSHIP_TYPE, Const.WORSHIP_TYPE_WEDNESDAY);
                newFragment.setArguments(bundle);
                break;
            case R.id.nav_worship_friday:
                newFragment = new SermonFragment();
                bundle = new Bundle();
                bundle.putString(Const.INTENT_KEY_TITLE, getString(R.string.worship_friday));
                bundle.putInt(Const.INTENT_KEY_WORSHIP_TYPE, Const.WORSHIP_TYPE_FRIDAY);
                newFragment.setArguments(bundle);
                break;
        }

        // notify to main fragment container for replace content
        switchContent(newFragment);
        hideSlideMenu();

        return true;
    }

    private void switchContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void hideSlideMenu() {
        if (mDrawerLayout == null) { return; }
        mDrawerLayout.closeDrawers();
    }

    public void addContent(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment)
                .addToBackStack("detail").commit();
    }

    private void releaseDialog(Dialog dialog) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private void showPushMessageIfNecessary(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Logger.e(TAG, "bundle is null");
            return;
        }

        final String message = bundle.getString(Const.INTENT_KEY_MESSAGE);
        if (!TextUtils.isEmpty(message)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        Intent intent = new Intent(MainActivity.this, PushMessageActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Const.INTENT_KEY_MESSAGE, message);
                        startActivity(intent);
                    } else {
                        Logger.i(TAG, "isFinishing() : " + isFinishing());
                    }
                }
            }, 3000);
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

    public interface PermissionCheckResultListener {
        void onResult(boolean isGranted);
    }


    public void startPermissionCheck(final String checkPermission, PermissionCheckResultListener listener) {
        if (listener == null) {
            Logger.e(TAG, "cannot check permission caused by listener is not valid");
            return;
        }

        if (TextUtils.isEmpty(checkPermission)) {
            Logger.e(TAG, "cannot check permission caused by wrong permission parameter");
            listener.onResult(false);
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Logger.i(TAG, "There is no need to check permission. The device platform version under Marshmallow");
            listener.onResult(true);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, checkPermission) != PackageManager.PERMISSION_GRANTED) {
            mPermissionResultListener = listener;

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, checkPermission)) {
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);

                String rationale;
                if (checkPermission.equals(Manifest.permission.READ_PHONE_STATE)) {
                    rationale = getString(R.string.rationale_read_phone_state);

                } else if (checkPermission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    rationale = getString(R.string.rationale_write_external_storage);

                } else {
                    Logger.e(TAG, "cannot check permission caused by checkPermission parameter is not valid");
                    mPermissionResultListener = null;
                    return;
                }

                ab.setTitle(R.string.require_permission);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        Logger.i(TAG, "onNewIntent - intent action : " + action);

        if (action.equals(Const.INTENT_ACTION_OPEN_CHAT)) {
            switchContent(new ChatFragment());
        }
    }
}