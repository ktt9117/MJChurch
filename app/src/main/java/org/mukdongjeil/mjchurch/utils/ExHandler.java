package org.mukdongjeil.mjchurch.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public abstract class ExHandler<T> extends Handler {
	
	private WeakReference<T> mReference;
	
	public ExHandler(T reference) {
		mReference = new WeakReference<>(reference);
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if(mReference.get() == null) {
			return;
		}
		handleMessage(mReference.get(), msg);
	}
	
	protected abstract void handleMessage(T reference, Message msg);
}
