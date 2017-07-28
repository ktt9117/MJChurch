package org.mukdongjeil.mjchurch.utils;

import android.util.Log;

import org.mukdongjeil.mjchurch.Const;

public class Logger {

	private static String _PREFIX = "[MJChurch] ";
	

	public static void setPrefix(String prefix) {
		_PREFIX = prefix;
	}

	private static String addPrefix(String msg) {
		return (_PREFIX + msg);
	}

	private static String addPrefixTag(String tag, String msg) {
		return (_PREFIX + "[" + tag + "] " + msg);
	}

	public static void w(String tag, String msg) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefix(msg);
			Log.w(tag, dbgMsg);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefix(msg);
			Log.w(tag, dbgMsg, tr);
		}
	}

	public static void e(String tag, String msg) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefix(msg);
			Log.e(tag, dbgMsg);
		}
	}

	public static void e(String tag, String msg, Exception e) {
		if (Const.DEBUG_MODE) {
			e.printStackTrace();
			String dbgMsg = addPrefix(msg);
			Log.e(tag, dbgMsg);
		}
	}

	public static void v(String tag, String msg) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefix(msg);
			Log.v(tag, dbgMsg);
		}
	}

	public static void d(String tag, String msg) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefix(msg);
			Log.d(tag, dbgMsg);
		}
	}

	public static void i(String tag, String msg) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefix(msg);
			Log.i(tag, dbgMsg);
		}
	}

	public static void i(String tag, String msg, Exception e) {
		if (Const.DEBUG_MODE) {
			e.printStackTrace();
			String dbgMsg = addPrefix(msg);
			Log.i(tag, dbgMsg);
		}
	}

	public static void v(String author, String tag, String msg) {
		if (Const.DEBUG_MODE) {
			String dbgMsg = addPrefixTag(tag, msg);
			Log.v(author, dbgMsg);
		}
	}

	public static boolean isLoggable() {
		return Const.DEBUG_MODE;
	}

	public static boolean isLoggable(String tag, int level) {
		return ((Const.DEBUG_MODE) && Log.isLoggable(tag, level));
	}
}
