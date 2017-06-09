package org.mukdongjeil.mjchurch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.mukdongjeil.mjchurch.common.util.Logger;

public class PushMessageActivity extends Activity {
    private static final String TAG = PushMessageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if (TextUtils.isEmpty(message)) {
            finish();
        }

        ((TextView) findViewById(R.id.tv_receive_message)).setText(message);
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.e(TAG, "okButtonClicked");
                finish();
            }
        });
    }
}
