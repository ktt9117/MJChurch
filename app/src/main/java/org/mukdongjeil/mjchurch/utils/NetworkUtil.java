package org.mukdongjeil.mjchurch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Kim SungJoong on 2015-09-14.
 */
public class NetworkUtil {

    public static final int NETWORK_NONE	= 0;
    public static final int NETWORK_WIFI	= 1;
    public static final int NETWORK_LTE		= 2;
    public static final int NETWORK_3G		= 3;

    public static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return "";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return (wifiInfo == null) ? "" : wifiInfo.getMacAddress();
    }

    public static int getNetwork(Context context) {
        ConnectivityManager mManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mManager.getActiveNetworkInfo();
        if(info != null && info.getTypeName() != null) {
            // 와이파이에 연결되었을때
            if (info.getTypeName().equalsIgnoreCase("WIFI")) {
                return NETWORK_WIFI;
            } else if (info.getTypeName().equalsIgnoreCase("mobile") && info.getSubtypeName() != null) {
                if (info.getSubtypeName().equalsIgnoreCase("LTE")) {	// LTE에 연결되었을때
                    return NETWORK_LTE;
                } else if(info.getSubtypeName().contains("CDMA")) { // 3G에 연결되었을때
                    return NETWORK_3G;
                }
            }
        }
        return NETWORK_NONE;	// 아무것에도 연결되지 않았을때
    }

    public static boolean isWifi(Context context) {
        return getNetwork(context) == NETWORK_WIFI;
    }

    public static boolean isMobileNetwork(Context context) {
        int network = getNetwork(context);
        return (network == NETWORK_LTE || network == NETWORK_3G);
    }


}
