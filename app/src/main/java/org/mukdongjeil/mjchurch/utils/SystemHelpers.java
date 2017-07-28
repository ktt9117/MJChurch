package org.mukdongjeil.mjchurch.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.List;

public class SystemHelpers {

	public static String getPhoneNumber(Context context) {
		TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String telNumber = telManager.getLine1Number();
		if(!TextUtils.isEmpty(telNumber) && telNumber.startsWith("+82")) {
			telNumber = telNumber.replace("+82", "0");
		}
		return telNumber;
	}
	
	public static String getPinCode(Context context) {
		String phoneNumber = getPhoneNumber(context);
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

	public static String getAppVersion(Context context) {
		PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            throw new IllegalStateException("getPackageInfo failed");
        }
        return packageInfo.versionName;
	}
}