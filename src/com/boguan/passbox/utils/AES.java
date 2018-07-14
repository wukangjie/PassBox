package com.boguan.passbox.utils;

import android.util.Log;

/**
 * AES加密工具
 */
public class AES {
    
    public static final String TAG = "AES";
    public static final String version = "20*";
    public static final String versionNumber = "20";
    public static final char delimiter = '*';
    public static final int seedLength = 8;
	/**
	 * AES加密
	 */
	public static String encrypt(String cleartext, String seed) throws Exception {
		try {
			AES2 aes = new AES2(seed);
			byte[] bytes = cleartext.getBytes("UTF8");
			return aes.encrypt(bytes);
		} catch (Exception e) {
			Log.e("P2PTransfer-CommonUtils", "Init Server Data Exception");
			e.printStackTrace();
		}
		return null;
	}
	
	public static String encrypt(String cleartext) throws Exception  {
	    StringRandom sr = StringRandom.getInstance();
        String seed = sr.getString(seedLength);
	    String encodeString = encrypt(cleartext, seed);
	    return (version+encodeString+seed);
	}

	/**
	 * AES解密
	 */
	
	public static String decrypt(String encrypted) throws Exception {
	    if (encrypted == null || encrypted.length() <= version.length()) {
            return encrypted;
        }
        String v = encrypted.substring(0, version.length());
        if (v.charAt(v.length()-1) != delimiter) {
            Log.e(TAG, encrypted + " <may be decrypt by seed> ");
            return encrypted;
        }

        String seed = encrypted.substring(encrypted.length()-seedLength);
        String pure = encrypted.substring(version.length(), encrypted.length()-seedLength-1);

		v = v.substring(0, version.length()-1);
		int r = v.compareTo(versionNumber);
		if (r > 0) {
			Log.e(TAG, encrypted + " <encrypt version is new, use the newest app please> ");
			return null;
		} else if (r < 0) {
			Log.e(TAG, encrypted + " <encrypt version is old, will be convert to newest version> ");
			return AES1.decrypt(pure, seed);
		}

		try {
			AES2 aes = new AES2(seed);
			return aes.decrypt(pure);
		} catch (Exception e) {
			Log.e("P2PTransfer-Client", "init client exception");
			e.printStackTrace();
		}
		return null;
    }

}
