package org.mukdongjeil.mjchurch.common.ext_view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import org.mukdongjeil.mjchurch.R;

public class CycleProgressDialog extends Dialog {

	private ImageView mCycleIcon;
	private Animation mRotateAni;
	
	public CycleProgressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	public CycleProgressDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public CycleProgressDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		init(context);
	}

	private void init(Context context) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setCanceledOnTouchOutside(false);
		
		View view = getLayoutInflater().inflate(R.layout.dialog_loading_cycle, null);
		setContentView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		
		mCycleIcon = (ImageView) view.findViewById(R.id.cycle_icon);
		mRotateAni = AnimationUtils.loadAnimation(context, R.anim.rotate_ani);
	}
	
	@Override
	public void show() {
		mCycleIcon.startAnimation(mRotateAni);
		super.show();
	}
	
	@Override
	public void dismiss() {
		mCycleIcon.clearAnimation();
		super.dismiss();
	}
}
