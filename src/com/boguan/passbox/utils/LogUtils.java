package com.boguan.passbox.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import android.util.Log;

/**
 * ログユーティリティクラス
 *
 */
public class LogUtils {

	/**
	 * コンストラクタ
	 */
	private LogUtils() {
	}
	
	/**
	 * ログ出力モードかどうか
	 * ログは本クラスを経由し出力すること。
	 * apkリリース時には必ずfalseに変更すること。
	 */
	private static final boolean LOG_MODE = true;
	private static final String TAG = "PassBox";

	public static void v(String msg) {
		outputLog(msg);
	}

	public static void e(String msg, Exception e) {
		outputLog(Log.ERROR, msg, e);
	}

	public static void w(String msg) {
		outputLog(Log.WARN, msg);
	}

	public static <T> void trace(T instance) {
		v(instance.getClass().getSimpleName() + ": " + Thread.currentThread().getStackTrace()[3].getMethodName());
	}
	
	/**
	 * ログ出力
	 * 必ずLog.DEBUGで出力
	 * @param msg ログメッセージ
	 */
	public static void outputLog(String msg) {
		outputLog(Log.DEBUG, msg, null);
	}
	
	/**
	 * ログ出力
	 * @param type ログ種別
	 * @param msg ログメッセージ
	 */
	public static void outputLog(int type, String msg) {
		outputLog(type, msg, null);
	}
	
	/**
	 * ログ出力
	 * @param type ログ種別
	 * @param msg ログメッセージ
	 * @param e 例外
	 */
	public static void outputLog(int type, String msg, Exception e) {
		if(!LOG_MODE) return;
		
		switch(type) {
			case Log.ASSERT:
				break;
			case Log.ERROR:
				Log.e(TAG, msg, e);
				break;
			case Log.WARN:
				Log.w(TAG, msg);
				break;
			case Log.DEBUG:
				Log.d(TAG, msg);
				break;
			case Log.INFO:
				Log.i(TAG, msg);
				break;
			case Log.VERBOSE:
				Log.v(TAG, msg);
				break;
			default:
				break;
		}
	}
	
	/**
	 * 指定したパスのファイルにログを出力する。
	 * @param directorypath
	 * @param msg
	 */
	public static void outputLog(String directorypath, String msg) {
		// ディレクトリが存在していなかったら、作成する
		File directory = new File(directorypath);
		if(!directory.isDirectory()) {
			directory.mkdirs();
		}
		
		File f = new File(directorypath + "/log.txt");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(f, true);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, Charset.defaultCharset());
			PrintWriter writer = new PrintWriter(outputStreamWriter);
			writer.append(msg+"\r\n");
			writer.flush();
			writer.close();
		} catch(Exception e) {
			outputLog(Log.ERROR, "outputLog : Exception", e); 
		}
	}
	
	/**
	 * debug mode
	 * @return
	 */
	public static boolean isDebug(){
	    return LOG_MODE;
	}
}
