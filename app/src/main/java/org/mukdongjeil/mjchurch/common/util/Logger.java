package org.mukdongjeil.mjchurch.common.util;

import android.util.Log;

public class Logger {

	private static boolean _DEBUG = true;
	private static String _PREFIX = "[MJChurch] ";

	public static void init(boolean debug) {
		Logger._DEBUG = debug;
	}

	public static void setPrefix(String prefix) {
		Logger._PREFIX = prefix;
	}

	private static String addPrefix(String msg) {
		return (Logger._PREFIX + msg);
	}

	private static String addPrefixnTag(String tag, String msg) {
		return (Logger._PREFIX + "[" + tag + "] " + msg);
	}

	public static void w(String tag, String msg) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefix(msg);
			Log.w(tag, dbgMsg);
		}
	}

	public static void w(String tag, String msg, Throwable tr) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefix(msg);
			Log.w(tag, dbgMsg, tr);
		}
	}

	public static void e(String tag, String msg) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefix(msg);
			Log.e(tag, dbgMsg);
		}
	}

	public static void e(String tag, String msg, Exception e) {
		if (Logger._DEBUG) {
			e.printStackTrace();
			String dbgMsg = addPrefix(msg);
			Log.e(tag, dbgMsg);
		}
	}

	public static void v(String tag, String msg) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefix(msg);
			Log.v(tag, dbgMsg);
		}
	}

	public static void d(String tag, String msg) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefix(msg);
			Log.d(tag, dbgMsg);
		}
	}

	public static void i(String tag, String msg) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefix(msg);
			Log.i(tag, dbgMsg);
		}
	}

	public static void i(String tag, String msg, Exception e) {
		if (Logger._DEBUG) {
			e.printStackTrace();
			String dbgMsg = addPrefix(msg);
			Log.i(tag, dbgMsg);
		}
	}

	public static void v(String author, String tag, String msg) {
		if (Logger._DEBUG) {
			String dbgMsg = addPrefixnTag(tag, msg);
			Log.v(author, dbgMsg);
		}
	}

	public static boolean isLoggable() {
		return Logger._DEBUG;
	}

	public static boolean isLoggable(String tag, int level) {
		return ((_DEBUG) && Log.isLoggable(tag, level));
	}
}
