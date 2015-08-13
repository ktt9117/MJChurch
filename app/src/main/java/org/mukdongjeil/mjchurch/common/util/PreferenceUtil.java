package org.mukdongjeil.mjchurch.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceUtil {
	
	private static final String TAG = PreferenceUtil.class.getSimpleName();

	public static final String PREF_NAME = "save_instance_pref"; // Preference Name

	private static final String INTRODUCE_LAST_SELECTED_MENU_INDEX = "introduceLastSelectedMenuIndex";
	private static final String WORSHIP_LAST_SELECTED_MENU_INDEX = "worshipLastSelectedMenuIndex";

	private static SharedPreferences mPreference = null;
	private static Context mContext = null;
	
	/**
	 * Singleton  
	 * @param context : getApplicationContext();
	 **/
	public static void init(Context context) {
		mContext = context;
		if (context != null) {
			mPreference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		}
	}
	
	// ------- Overloading Method (Set preference data) ------- //
	private static void setPreferenceValue(String preName, boolean flag) {
		Editor editor = mPreference.edit();
		editor.putBoolean(preName, flag);
		editor.commit();
	}
	
	private static void setPreferenceValue(String preName, String str) {
		Editor editor = mPreference.edit();
		editor.putString(preName, str);
		editor.commit();
	}
	
	private static void setPreferenceValue(String preName, int value) {
		Editor editor = mPreference.edit();
		editor.putInt(preName, value);
		editor.commit();
	}
	
	private static void setPreferenceValue(String preName, long value) {
		Editor editor = mPreference.edit();
		editor.putLong(preName, value);
		editor.commit();
	}
	
	// ---------------------- Setter -------------------------- //
	public static void setIntroduceLastSelectedMenuIndex(int index) {
		setPreferenceValue(INTRODUCE_LAST_SELECTED_MENU_INDEX, index);
	}

	public static void setWorshipLastSelectedMenuIndex(int index) {
		setPreferenceValue(WORSHIP_LAST_SELECTED_MENU_INDEX, index);
	}

	// ---------------------- Getter -------------------------- //

	public static int getIntroduceLastSelectedMenuIndex() {
		return mPreference.getInt(INTRODUCE_LAST_SELECTED_MENU_INDEX, 0);
	}

	public static int getWorshipLastSelectedMenuIndex() {
		return mPreference.getInt(WORSHIP_LAST_SELECTED_MENU_INDEX, 0);
	}
}
