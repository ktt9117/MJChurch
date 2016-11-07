package org.mukdongjeil.mjchurch.common.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtil {
	
	/**
	 * This method convets dp unit to equivalent device specific value in pixels. 
	 * 
	 * @param dp A value in dp(Device independent pixels) unit. Which we need to convert into pixels
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent Pixels equivalent to dp according to device
	 */
	public static int convertDpToPixel(float dp){
		Resources resources = SystemHelpers.getApplicationContext().getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi/160f);
		return Math.round(px);
	}
	
	/**
	 * This method converts device specific pixels to device independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent db equivalent to px value
	 */
	public static float convertPixelsToDp(float px){
	    Resources resources = SystemHelpers.getApplicationContext().getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    return px / (metrics.densityDpi / 160f);
	}

	public static int getDisplaySizeWidth(Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if (display != null) {
			Point size = new Point();
			display.getSize(size);
			return size.x;
		} else {
			return 0;
		}
	}
	
	public static int getDisplaySizeHeight(Context context) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if (display != null) {
			Point size = new Point();
			display.getSize(size);
			return size.y;
		} else {
			return 0;
		}
	}
}