package org.mukdongjeil.mjchurch.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class StringUtils {

	public static final String FILE_EXTENSION_MP3 = ".mp3";

	public static final String PREFIX_FILE_URL = "file://";
	
	public static boolean isUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
	
	public static boolean isFileUri(String str) {
    	return str.startsWith(PREFIX_FILE_URL);
    }
	
	public static String setPrefixforFileUri(String str) {
    	if (!isFileUri(str)) {
    		return PREFIX_FILE_URL + str;
    	}

    	return str;
    }
	
	/*
	 * 01.01.01 형식의 버전이름을 1.1.1 형식으로 바꿔서 리턴
	 */
	public static String convertToDisplayVersionCode(String versionName) {
		String[] versions = versionName.split("[.]");
		StringBuilder sb = new StringBuilder();
		if (versions.length == 3) {
			for(int i = 0; i < 3; i++) {
				if(Integer.parseInt(versions[i]) < 10) {
					versions[i] = versions[i].replace("0", "");
				}
				sb.append(versions[i]);
				if(i != 2) {
					sb.append(".");
				}
			}			
		}

		return sb.toString();
	}

	public static String removeDuplicationSentence(String original) {
		Logger.e("removeDuplicationSentence", "original : " + original);
		if (TextUtils.isEmpty(original)) {
			Logger.e("removeDuplicationSentence", "original text is empty. just return original");
			return original;
		}

		if (original.length() < 10) {
			Logger.e("removeDuplicationSentence", "original length under 10 just return original");
			return original;
		}

		String sentencePrefix = original.substring(0, 10);
		Logger.e("removeDuplicationSentence", "sentencePrefix : " + sentencePrefix);
		int duplicationIndex = original.indexOf(sentencePrefix, 1);
		Logger.e("removeDuplicationSentence", "duplicationIndex : " + duplicationIndex);
		if (duplicationIndex > 0) {
			String result = original.substring(0, duplicationIndex - 1);
			Logger.e("removeDuplicationSentence", "result : " + result);
			return result;
		}
		Logger.e("removeDuplicationSentence", "There is no duplication sentence. just return original");
		return original;
	}
}
