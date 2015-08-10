package org.mukdongjeil.mjchurch.common.util;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public abstract class ExHandler<T> extends Handler {
	
	private WeakReference<T> mReference;
	
	public ExHandler(T reference) {
		mReference = new WeakReference<T>(reference);
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		if(mReference.get() == null) {
			return;
		}
		handleMessage(mReference.get(), msg);
	}
	
	protected abstract void handleMessage(T reference, Message msg);
}
