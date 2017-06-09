package org.mukdongjeil.mjchurch.common.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.mukdongjeil.mjchurch.common.Const;

public class PreferenceUtil {
	
	private static final String TAG = PreferenceUtil.class.getSimpleName();

	public static final String PREF_NAME = "save_instance_pref"; // Preference Name

	private static final String INTRODUCE_LAST_SELECTED_MENU_INDEX = "introduceLastSelectedMenuIndex";
	private static final String WORSHIP_LAST_SELECTED_MENU_INDEX = "worshipLastSelectedMenuIndex";

	private static final String SUNDAY_MORNING_WORSHIP_CHECK_DATE = "sundayMorningWorshipCheckDate";
	private static final String SUNDAY_AFTERNOON_WORSHIP_CHECK_DATE = "sundayAfternoonWorshipCheckDate";
	private static final String WEDNESDAY_WORSHIP_CHECK_DATE = "WednesdayWorshipCheckDate";

	private static final String BOARD_CHECK_DATE = "boardListCheckDate";
	private static final String GALLERY_CHECK_DATE = "galleryListCheckDate";
	private static final String GALLERY_NEW_PERSON_CHECK_DATE = "galleryNewPersonListCheckDate";

	private static SharedPreferences mPreference = null;

	/**
	 * Singleton  
	 * @param context : getApplicationContext();
	 **/
	public static void init(Context context) {
		if (context != null) {
			mPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		}
	}
	
	// ------- Overloading Method (Set preference data) ------- //
	private static void setPreferenceValue(String preName, boolean flag) {
		Editor editor = mPreference.edit();
		editor.putBoolean(preName, flag);
		editor.apply();
	}
	
	private static void setPreferenceValue(String preName, String str) {
		Editor editor = mPreference.edit();
		editor.putString(preName, str);
		editor.apply();
	}
	
	private static void setPreferenceValue(String preName, int value) {
		Editor editor = mPreference.edit();
		editor.putInt(preName, value);
		editor.apply();
	}
	
	private static void setPreferenceValue(String preName, long value) {
		Editor editor = mPreference.edit();
		editor.putLong(preName, value);
		editor.apply();
	}

	// ---------------------- Setter -------------------------- //
	public static void setIntroduceLastSelectedMenuIndex(int index) {
		setPreferenceValue(INTRODUCE_LAST_SELECTED_MENU_INDEX, index);
	}

	public static void setWorshipLastSelectedMenuIndex(int index) {
		setPreferenceValue(WORSHIP_LAST_SELECTED_MENU_INDEX, index);
	}

	public static void setWorshipListCheckTimeInMillis(int worshipType, long currentTimeInMillis) {
		if (worshipType == Const.WORSHIP_TYPE_SUNDAY_MORNING) {
			setPreferenceValue(SUNDAY_MORNING_WORSHIP_CHECK_DATE, currentTimeInMillis);
		} else if (worshipType == Const.WORSHIP_TYPE_SUNDAY_AFTERNOON) {
			setPreferenceValue(SUNDAY_AFTERNOON_WORSHIP_CHECK_DATE, currentTimeInMillis);
		} else {
			setPreferenceValue(WEDNESDAY_WORSHIP_CHECK_DATE, currentTimeInMillis);
		}
	}

	public static void setBoardListCheckTimeInMillis(long currentTimeInMillis) {
		setPreferenceValue(BOARD_CHECK_DATE, currentTimeInMillis);
	}

	public static void setGalleryListCheckTimeInMillis(long currentTimeInMillis) {
		setPreferenceValue(GALLERY_CHECK_DATE, currentTimeInMillis);
	}

	public static void setGalleryNewPersonListTimeInMillis(long currentTimeInMillis) {
		setPreferenceValue(GALLERY_NEW_PERSON_CHECK_DATE, currentTimeInMillis);
	}

	// ---------------------- Getter -------------------------- //

	public static int getIntroduceLastSelectedMenuIndex() {
		return mPreference.getInt(INTRODUCE_LAST_SELECTED_MENU_INDEX, 0);
	}

	public static int getWorshipLastSelectedMenuIndex() {
		return mPreference.getInt(WORSHIP_LAST_SELECTED_MENU_INDEX, 0);
	}

	public static long getWorshipListCheckTimeInMillis(int worshipType) {
		if (worshipType == Const.WORSHIP_TYPE_SUNDAY_MORNING) {
			return mPreference.getLong(SUNDAY_MORNING_WORSHIP_CHECK_DATE, 0);
		} else if (worshipType == Const.WORSHIP_TYPE_SUNDAY_AFTERNOON) {
			return mPreference.getLong(SUNDAY_AFTERNOON_WORSHIP_CHECK_DATE, 0);
		} else {
			return mPreference.getLong(WEDNESDAY_WORSHIP_CHECK_DATE, 0);
		}
	}

	public static long getBoardListCheckTimeInMillis() {
		return mPreference.getLong(BOARD_CHECK_DATE, 0);
	}

	public static long getGalleryListCheckTimeInMillis() {
		return mPreference.getLong(BOARD_CHECK_DATE, 0);
	}

	public static long getGalleryNewPersonListTimeInMillis() {
		return mPreference.getLong(BOARD_CHECK_DATE, 0);
	}

}
