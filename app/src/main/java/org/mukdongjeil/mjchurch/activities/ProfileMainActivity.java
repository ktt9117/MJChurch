package org.mukdongjeil.mjchurch.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.databinding.ActivityProfileMainBinding;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.Logger;

/**
 * Created by gradler on 27/07/2017.
 */

public class ProfileMainActivity extends BaseActivity {
    private static final String TAG = ProfileMainActivity.class.getSimpleName();
    private static final int RC_CHANGE_NAME = 1000;
    private static final int RC_CHANGE_PHOTO = 1001;

    private FirebaseAuth mAuth;
    private ActivityProfileMainBinding mBinding;

    private ExHandler<ProfileMainActivity> mHandler = new ExHandler<ProfileMainActivity>(this) {
        @Override
        protected void handleMessage(ProfileMainActivity reference, Message msg) {
            if (msg.obj instanceof String && reference.mBinding.profileAvatarView != null) {
                String loadUrl = (String) msg.obj;
                Glide.with(reference)
                        .load(loadUrl)
                        .error(R.drawable.ic_account_circle_black_36dp)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .into(reference.mBinding.profileAvatarView);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile_main);
        mBinding.setActivity(this);
        setTitle(R.string.config_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_CHANGE_NAME) {
                if (data != null && data.hasExtra(Const.INTENT_KEY_USERNAME)) {
                    String changedUsername = data.getStringExtra(Const.INTENT_KEY_USERNAME);
                    Logger.i(TAG, "changedUsername : " + changedUsername);
                    if (!TextUtils.isEmpty(changedUsername)) {
                        changeProfile(mAuth.getCurrentUser(), changedUsername);
                    }
                }
            } else if (requestCode == RC_CHANGE_PHOTO) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + (uri != null ? uri.toString() : uri));
                    if (uri == null) {
                        Logger.e(TAG, "uri is null");
                        return;
                    }

                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference(mAuth.getCurrentUser().getUid())
                            .child(uri.getLastPathSegment());
                    putImageIntoStorage(storageReference, uri);
                }
            }
        }
    }


    public void onUsernameClicked(View view) {
        Logger.e(TAG, "profile_username clicked");
        Intent nameChangeIntent = new Intent(this, ProfileNameActivity.class);
        nameChangeIntent.putExtra(Const.INTENT_KEY_USERNAME, mBinding.profileUsername.getText().toString());
        startActivityForResult(nameChangeIntent, RC_CHANGE_NAME);
    }

    public void onAvatarViewClicked(View view) {
        Logger.e(TAG, "profile_btn_camera or profile_avatar_view clicked");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(Const.MIME_TYPE_IMAGES);
        startActivityForResult(intent, RC_CHANGE_PHOTO);
    }

    private void updateProfileUI(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(getApplicationContext(), R.string.profile_inquire_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        String name = user.getDisplayName();
        String email = user.getEmail();
        Glide.with(ProfileMainActivity.this)
                .load(user.getPhotoUrl().getPath())
                .placeholder(R.drawable.ic_account_circle_black_36dp)
                .error(R.drawable.ic_account_circle_black_36dp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mBinding.profileAvatarView);

        mBinding.profileUsername.setText(TextUtils.isEmpty(name) ? email : name);
    }

    private void changeProfile(FirebaseUser user, final String displayName) {
        if (user == null) {
            return;
        }

        if (TextUtils.isEmpty(displayName)) {
            Logger.e(TAG, "cannot change profile caused by parameters are not valid");
            return;
        }

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        showLoadingDialog();
        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideLoadingDialog();

                if (task.isSuccessful()) {
                    mBinding.profileUsername.setText(displayName);
                    Toast.makeText(ProfileMainActivity.this, R.string.username_updated,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileMainActivity.this, "대화명 변경 실패 : " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void changeProfileImage(FirebaseUser user, final Uri photoUri) {
        if (user == null) {
            return;
        }

        if (photoUri == null || TextUtils.isEmpty(photoUri.toString())) {
            Logger.e(TAG, "cannot change profile caused by parameters are not valid");
            return;
        }

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(photoUri)
                .build();

        showLoadingDialog();
        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideLoadingDialog();

                if (task.isSuccessful()) {
                    Message message = mHandler.obtainMessage();
                    message.obj = photoUri.toString();
                    message.what = 0;
                    mHandler.sendMessage(message);

                    Toast.makeText(ProfileMainActivity.this, R.string.profile_image_updated,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileMainActivity.this, "프로필 사진 변경 실패 : " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void putImageIntoStorage(StorageReference storageReference, Uri uri) {
        showLoadingDialog();
        storageReference.putFile(uri).addOnCompleteListener(this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                hideLoadingDialog();
                if (task.isSuccessful()) {
                    Uri photoUri = task.getResult().getDownloadUrl();
                    changeProfileImage(mAuth.getCurrentUser(), photoUri);

                } else {
                    Toast.makeText(ProfileMainActivity.this, "프로필 사진 변경 실패 : " + task.getException(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Image upload task was not successful.",
                            task.getException());
                }
            }
        });
    }

    private void doLogout() {
        mAuth.signOut();
        finish();
    }
}
