package org.mukdongjeil.mjchurch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by John Kim on 2016-02-08.
 */
public class IntroActivity extends Activity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mImageView = new ImageView(getBaseContext());
        //mImageView.setImageResource(R.drawable.intro);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        mImageView.setBackgroundColor(Color.TRANSPARENT);
        setContentView(mImageView);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_ani);
        mImageView.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    finish();
                }
            }
        }, 2000);
    }
}
