package org.mukdongjeil.mjchurch.common.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.List;

public class SystemHelpers {

	private static Context mContext = null;
	
	public static void init(Context context) {
		mContext = context;
	}
	
	public static Context getApplicationContext() {
		return mContext;
	}
	
	public static String getPhoneNumber() {
		TelephonyManager telManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String telNumber = telManager.getLine1Number();
		if(!TextUtils.isEmpty(telNumber) && telNumber.startsWith("+82")) {
			telNumber = telNumber.replace("+82", "0");
		}
		return telNumber;
	}
	
	public static String getPinCode() {
		String phoneNumber = getPhoneNumber();
		if (!TextUtils.isEmpty(phoneNumber) &&
				phoneNumber.length() > 4) {
			return phoneNumber.substring(phoneNumber.length()-4, phoneNumber.length());
		}
		return null;
	}

	
	public static boolean isServiceRunning(Context context, String className) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (className == null || className.length() <= 0) {
			return false;
		}
		
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		if(activityManager != null) {
			List<RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
	
			if(serviceList != null) {
				for (RunningServiceInfo service : serviceList){
					if (service != null && 
							service.service.getClassName().equals(className)){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static String getSdkVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getAppVersion() {
		PackageInfo packageInfo;
        try {
            packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            throw new IllegalStateException("getPackageInfo failed");
        }
        return packageInfo.versionName;
	}
}