package org.mukdongjeil.mjchurch.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.services.BaseActivity;
import org.mukdongjeil.mjchurch.utils.Logger;

import agency.tango.android.avatarview.views.AvatarView;
import agency.tango.android.avatarviewglide.GlideLoader;

/**
 * Created by gradler on 27/07/2017.
 */

public class ProfileMainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = ProfileMainActivity.class.getSimpleName();
    private static final int RC_CHANGE_NAME = 1000;
    private static final int RC_CHANGE_PHOTO = 1001;

    private FirebaseAuth mAuth;
    private AvatarView mAvatarView;
    private TextView mUsernameField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_main);
        setTitle(R.string.config_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mAvatarView = (AvatarView) findViewById(R.id.profile_avatar_view);
        mUsernameField = (TextView) findViewById(R.id.profile_username);
        mUsernameField.setOnClickListener(this);
        mAvatarView.setOnClickListener(this);
        findViewById(R.id.profile_btn_camera).setOnClickListener(this);
        updateProfileUI(mAuth.getCurrentUser());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_btn_logout) {
            doLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.profile_username:
                Logger.e(TAG, "profile_username clicked");
                Intent nameChangeIntent = new Intent(this, ProfileNameActivity.class);
                nameChangeIntent.putExtra(Const.INTENT_KEY_USERNAME, mUsernameField.getText().toString());
                startActivityForResult(nameChangeIntent, RC_CHANGE_NAME);
                break;
            case R.id.profile_avatar_view:
            case R.id.profile_btn_camera:
                Logger.e(TAG, "profile_btn_camera or profile_avatar_view clicked");
                Toast.makeText(this, R.string.not_supported_yet, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_CHANGE_NAME) {
                if (data != null && data.hasExtra(Const.INTENT_KEY_USERNAME)) {
                    String changedUsername = data.getStringExtra(Const.INTENT_KEY_USERNAME);
                    Logger.i(TAG, "changedUsername : " + changedUsername);
                    changeProfile(mAuth.getCurrentUser(), changedUsername, null);
                }
            }
        }
    }

    private void updateProfileUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Logger.d(TAG, "currentUser photoUrl: " + user.getPhotoUrl() + ", username : " +
                    user.getDisplayName() + ", email : " + user.getEmail());

            GlideLoader loader = new GlideLoader();
            loader.loadImage(mAvatarView, user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString(),
                    TextUtils.isEmpty(name) ? email : name);

            mUsernameField.setText(TextUtils.isEmpty(name) ? email : name);
        }
    }

    private void changeProfile(FirebaseUser user, final String displayName, Uri photoUri) {
        if (user == null) {
            return;
        }

        showLoadingDialog();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(photoUri)
                .build();

        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AlertDialog dialog = new AlertDialog.Builder(ProfileMainActivity.this)
                            .setTitle(R.string.notification)
                            .setMessage(R.string.alert_message_profile_change)
                            .setPositiveButton(R.string.do_logout, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    doLogout();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
                    dialog.show();
                } else {
                    Toast.makeText(ProfileMainActivity.this, "프로필 변경 실패 : " + task.getException(), Toast.LENGTH_SHORT).show();
                }

                updateProfileUI(mAuth.getCurrentUser());
                hideLoadingDialog();
            }
        });
    }

    private void doLogout() {
        mAuth.signOut();
        finish();
    }
}
