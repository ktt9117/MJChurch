package org.mukdongjeil.mjchurch.common.util;

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
    	if(!isFileUri(str)) {
    		return PREFIX_FILE_URL + str;
    	}
    	return str;
    }
	
	/*
	 * 01.01.01 형식의 버전이름을 1.1.1 형식으로 바꿔서 리턴
	 */
	public static String convertToDisplayVersionCode(String versionName) {
		String[] versions = versionName.split("[.]");
		StringBuffer sb = new StringBuffer();
		if(versions != null && versions.length == 3) {			
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
}
