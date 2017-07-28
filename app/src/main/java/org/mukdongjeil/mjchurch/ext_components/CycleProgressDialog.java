package org.mukdongjeil.mjchurch.ext_components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout.LayoutParams;

import org.mukdongjeil.mjchurch.R;

public class CycleProgressDialog extends Dialog {

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
		Window window = getWindow();
		if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		setCanceledOnTouchOutside(false);

		final ViewGroup nullViewGroup = null;
		View view = getLayoutInflater().inflate(R.layout.dialog_loading_cycle, nullViewGroup);
		setContentView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	}
	
	@Override
	public void show() {
		super.show();
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
	}
}
