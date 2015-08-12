package org.mukdongjeil.mjchurch.common.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class SystemHelpers {
	
	public static final int NETWORK_NONE	= 0;
	public static final int NETWORK_WIFI	= 1;
	public static final int NETWORK_LTE		= 2;
	public static final int NETWORK_3G		= 3;
	
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
	
	public static String getMacAddress() {
		if (mContext == null) {
			throw new NullPointerException();
		}
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager == null) {
			return "";
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();		
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
	
	public static int getNetwork() {
		ConnectivityManager mManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mManager.getActiveNetworkInfo();
		if(info != null && info.getTypeName() != null) {
			// 와이파이에 연결되었을때
			if (info.getTypeName().equalsIgnoreCase("WIFI")) {
				return NETWORK_WIFI;
			} else if (info.getTypeName().equalsIgnoreCase("mobile") && info.getSubtypeName() != null) {
				if (info.getSubtypeName().equalsIgnoreCase("LTE")) {	// LTE에 연결되었을때
					return NETWORK_LTE;
				} else if(info.getSubtypeName().indexOf("CDMA") >= 0) { // 3G에 연결되었을때
					return NETWORK_3G;
				}
			}
		}
		return NETWORK_NONE;	// 아무것에도 연결되지 않았을때
	}
	
	public static boolean isWifi() {
		return getNetwork() == NETWORK_WIFI;
	}
	
	public static boolean isMobileNetwork() {
		int network = getNetwork();
		return network == NETWORK_LTE || network == NETWORK_3G;
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