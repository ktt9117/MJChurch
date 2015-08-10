package org.mukdongjeil.mjchurch.common.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ExViewPager extends ViewPager {

	public ExViewPager(Context context) {
		super(context);
	}

	public ExViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		try {
			boolean ret = super.onInterceptTouchEvent(event);
			if(ret) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			return ret;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		boolean ret = super.onTouchEvent(arg0);
		if(ret) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		return ret; 
	}
}
