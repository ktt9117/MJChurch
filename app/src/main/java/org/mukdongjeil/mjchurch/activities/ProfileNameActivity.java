package org.mukdongjeil.mjchurch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.Const;
import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.ext_components.ClearableEditText;

/**
 * Created by gradler on 27/07/2017.
 */

public class ProfileNameActivity extends BaseActivity {
    private static final String TAG = ProfileNameActivity.class.getSimpleName();

    private String mOriginUsername;
    private ClearableEditText mUsernameField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_name);

        mOriginUsername = getIntent().getStringExtra(Const.INTENT_KEY_USERNAME);
        if (TextUtils.isEmpty(mOriginUsername)) {
            Toast.makeText(getApplicationContext(), R.string.invalid_call_activity, Toast.LENGTH_SHORT).show();
            finish();
        }

        setTitle(R.string.change_username);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsernameField = (ClearableEditText) findViewById(R.id.profile_edt_username);
        mUsernameField.setText(mOriginUsername);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        if (menuItemId == android.R.id.home) {
            finish();
            return true;

        } else if (menuItemId == R.id.action_btn_ok) {
            if (TextUtils.isEmpty(mOriginUsername)) {
                return super.onOptionsItemSelected(item);
            }

            String changedUsername = mUsernameField.getText().toString();
            if (TextUtils.isEmpty(changedUsername)) {
                mUsernameField.setError(getString(R.string.username_required));
                return super.onOptionsItemSelected(item);
            }

            mUsernameField.setError(null);

            if (!mOriginUsername.equals(changedUsername)) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(Const.INTENT_KEY_USERNAME, changedUsername);
                setResult(RESULT_OK, resultIntent);
            }

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
