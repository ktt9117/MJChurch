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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.databinding.ActivityProfileMainBinding;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.models.User;
import org.mukdongjeil.mjchurch.services.FirebaseDataHelper;
import org.mukdongjeil.mjchurch.utils.ExHandler;
import org.mukdongjeil.mjchurch.utils.Logger;

/**
 * Created by gradler on 27/07/2017.
 */

public class ProfileMainActivity extends BaseActivity {
    private static final String TAG = ProfileMainActivity.class.getSimpleName();
    private static final int RC_CHANGE_NAME = 1000;
    private static final int RC_CHANGE_PHOTO = 1001;

    private DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().getRoot();
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
        setTitle(R.string.config_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        updateProfileUI(mAuth.getCurrentUser().getEmail());
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

                    showLoadingDialog();
                    FirebaseDataHelper.getUserByEmail(mRef, mAuth.getCurrentUser().getEmail(),
                            new FirebaseDataHelper.OnUserQueryListener() {
                                @Override
                                public void onResult(User user) {
                                    if (user != null) {
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(mAuth.getCurrentUser().getUid())
                                                        .child(uri.getLastPathSegment());
                                        putImageInStorage(storageReference, uri, user);

                                    } else {
                                        hideLoadingDialog();
                                        Logger.e(TAG, "There is no user for updating profile image");
                                    }
                                }
                            }
                    );
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
        Toast.makeText(this, R.string.not_supported_yet, Toast.LENGTH_SHORT).show();
    }

    private void updateProfileUI(String email) {
        if (email == null) {
            return;
        }

        FirebaseDataHelper.getUserByEmail(mRef, email, new FirebaseDataHelper.OnUserQueryListener() {
            @Override
            public void onResult(User user) {
                if (user != null) {
                    String name = user.name;
                    String email = user.email;
                    if (TextUtils.isEmpty(user.photoUrl)) {
                        Glide.with(ProfileMainActivity.this)
                                .load(R.drawable.ic_account_circle_black_36dp)
                                .into(mBinding.profileAvatarView);
                    } else {
                        Glide.with(ProfileMainActivity.this)
                                .load(user.photoUrl)
                                .placeholder(R.drawable.ic_account_circle_black_36dp)
                                .error(R.drawable.ic_account_circle_black_36dp)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(mBinding.profileAvatarView);
                    }

                    mBinding.profileUsername.setText(TextUtils.isEmpty(name) ? email : name);

                } else {
                    Toast.makeText(getApplicationContext(), R.string.profile_inquire_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void changeProfile(FirebaseUser user, final String displayName) {
        if (user == null) {
            return;
        }

        if (TextUtils.isEmpty(displayName)) {
            Logger.e(TAG, "cannot change profile caused by parameters are not valid");
            return;
        }

        showLoadingDialog();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        final String targetEmail = user.getEmail();

        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseDataHelper.getUserByEmail(mRef, targetEmail,
                            new FirebaseDataHelper.OnUserQueryListener() {
                        @Override
                        public void onResult(User user) {
                            if (user != null) {
                                user.name = displayName;
                                mBinding.profileUsername.setText(displayName);
                                FirebaseDataHelper.createOrUpdateUser(mRef, user);
                                Toast.makeText(ProfileMainActivity.this, R.string.username_updated,
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(ProfileMainActivity.this, R.string.update_username_failed,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(ProfileMainActivity.this, "대화명 변경 실패 : " + task.getException(), Toast.LENGTH_SHORT).show();
                }

                hideLoadingDialog();
            }
        });
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final User user) {
        storageReference.putFile(uri).addOnCompleteListener(this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    @SuppressWarnings("VisibleForTests")
                    String uploadedUrl = task.getResult().getDownloadUrl().toString();
                    user.photoUrl = uploadedUrl;
                    FirebaseDataHelper.createOrUpdateUser(mRef, user);

                    Message message = mHandler.obtainMessage();
                    message.obj = uploadedUrl;
                    message.what = 0;
                    mHandler.sendMessage(message);

                } else {
                    Log.w(TAG, "Image upload task was not successful.",
                            task.getException());
                }

                hideLoadingDialog();
            }
        });
    }

    private void doLogout() {
        mAuth.signOut();
        finish();
    }
}
