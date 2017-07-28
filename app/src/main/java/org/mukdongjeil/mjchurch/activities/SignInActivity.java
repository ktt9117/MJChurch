package org.mukdongjeil.mjchurch.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.ext_components.ClearableEditText;
import org.mukdongjeil.mjchurch.utils.Logger;
import org.mukdongjeil.mjchurch.utils.PreferenceUtil;
import org.mukdongjeil.mjchurch.services.BaseActivity;

public class SignInActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = SignInActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;

    private ClearableEditText mUsernameField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getSupportActionBar().hide();

        mUsernameField = (ClearableEditText) findViewById(R.id.edt_email);
        mPasswordField = (EditText) findViewById(R.id.edt_password);
        findViewById(R.id.btn_email_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_google_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_sign_up).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        mAuth = FirebaseAuth.getInstance();

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            showAlert(R.string.login_unavailable, R.string.google_play_unavailable, true);
        }

        String savedMailAddress = PreferenceUtil.getSavedEmail();
        if (!TextUtils.isEmpty(savedMailAddress)) {
            mUsernameField.setText(savedMailAddress);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(TAG, "onActivityResult requestCode : " + requestCode + ", resultCode : " + resultCode);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                Logger.e(TAG, "Login unsuccessful result status : " + result.getStatus());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_sign_up:
            signUpWithEmail(mUsernameField.getText().toString(), mPasswordField.getText().toString());
            break;
        case R.id.btn_email_sign_in:
            signInWithEmail(mUsernameField.getText().toString(), mPasswordField.getText().toString());
            break;
        case R.id.btn_google_sign_in:
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
            break;
        }
    }

    private void signUpWithEmail(final String email, String password) {
        if (!validateForm()) {
            return;
        }

        showLoadingDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Logger.d(TAG, "createUserWithEmail:success");
                            PreferenceUtil.setEmail(email);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Logger.w(TAG, "createUserWithEmail:failure", task.getException());
                            showAlert(R.string.auth_failure, "인증 실패 : " + task.getException().getLocalizedMessage(), false);
                        }

                        hideLoadingDialog();
                    }
                });
    }

    private void signInWithEmail(final String email, String password) {
        if (!validateForm()) {
            return;
        }

        showLoadingDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            PreferenceUtil.setEmail(email);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Logger.e(TAG, "signInWithEmail:failure : " + task.getException());
                            showAlert(R.string.auth_failure, "인증 실패 : " + task.getException().getLocalizedMessage(), false);
                        }

                        hideLoadingDialog();
                    }
                });

    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Logger.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showLoadingDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Logger.e(TAG, "signInWithCredential:success");
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Logger.e(TAG, "signInWithCredential:failure", task.getException());
                            showAlert(R.string.auth_failure, "인증 실패 : " + task.getException().getLocalizedMessage(), false);
                        }

                        hideLoadingDialog();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mUsernameField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mUsernameField.setError(getString(R.string.email_required));
            valid = false;
        } else {
            mUsernameField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.password_required));
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void showAlert(int title, int message, boolean needToFinish) {
        showAlert(getString(title), getString(message), needToFinish);
    }

    private void showAlert(int title, String message, boolean needToFinish) {
        showAlert(getString(title), message, needToFinish);
    }

    private void showAlert(String title, String message, final boolean needToFinish) {
        AlertDialog dialog = new AlertDialog.Builder(SignInActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (needToFinish) {
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                    }
                })
                .create();
        dialog.show();
    }
}