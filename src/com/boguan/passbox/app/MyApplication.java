package com.boguan.passbox.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Looper;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.utils.AES;
import com.boguan.passbox.utils.Anime;

public class MyApplication extends Application implements OnSharedPreferenceChangeListener {
	/** 配置文件 */
	private SharedPreferences sharedPreferences;
	private Map<SettingKey, List<OnSettingChangeListener>> onSettingChangeListenerMap = new HashMap<SettingKey, List<OnSettingChangeListener>>();

	private boolean alreadyLeftFromForeground;
	private boolean passwordChanged;


	@Override
	public void onCreate() {
		super.onCreate();
		loadSettings();
		Anime.init(this);
	}

	private void loadSettings() {
		sharedPreferences = getSharedPreferences("settings", Context.MODE_MULTI_PROCESS);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * 获取设置
	 * 
	 * @param key
	 *            设置key
	 * @param defValue
	 *            没有该设置将要返回的默认值
	 * @return
	 */
	public String getSetting(SettingKey key, String defValue) {
		return sharedPreferences.getString(key.name(), defValue);
	}

	/**
	 * 保存设置，调用该方法后会产生
	 * {@link OnSettingChangeListener#onSettingChange(SettingKey)}回调。
	 * 
	 * @param key
	 *            设置保存key
	 * @param value
	 *            需要保存的值
	 */
	public void putSetting(SettingKey key, String value) {
		sharedPreferences.edit().putString(key.name(), value).commit();
	}
	
	public void putSettingEncode(SettingKey key, String value) {
        try {
            String encodeVaule = AES.encrypt(value);
            putSetting(key, encodeVaule);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public String getSettingDecode(SettingKey key, String defValue) {
        try {
            
            String value = getSetting(key, defValue);
            String encodeVaule = AES.decrypt(value);
            return encodeVaule;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return defValue;
    }

	/**
	 * 注册设置改变监听器，该方法必须在主线程中调用，且不用时必须调用
	 * {@link #unregistOnSettingChangeListener(SettingKey, OnSettingChangeListener)}
	 * 
	 * @param key
	 *            需要监听的设置项
	 * @param onSettingChangeListener
	 *            监听变化的回调
	 */
	public void registOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
		checkUIThread();

		List<OnSettingChangeListener> onSettingChangeListeners;
		if (onSettingChangeListenerMap.containsKey(key)) {
			onSettingChangeListeners = onSettingChangeListenerMap.get(key);
		} else {
			onSettingChangeListeners = new ArrayList<OnSettingChangeListener>();
			onSettingChangeListenerMap.put(key, onSettingChangeListeners);
		}
		onSettingChangeListeners.add(onSettingChangeListener);
	}

	/**
	 * 注销设置变化监听，该方法和
	 * {@link #registOnSettingChangeListener(SettingKey, OnSettingChangeListener)}
	 * 配套使用
	 * 
	 * @param key
	 *            需要注销的设置选项
	 * @param onSettingChangeListener
	 *            监听器
	 */
	public void unregistOnSettingChangeListener(SettingKey key, OnSettingChangeListener onSettingChangeListener) {
		checkUIThread();
		if (onSettingChangeListenerMap.containsKey(key)) {
			List<OnSettingChangeListener> onSettingChangeListeners = onSettingChangeListenerMap.get(key);
			onSettingChangeListeners.remove(onSettingChangeListener);
			if (onSettingChangeListeners.size() == 0) {
				onSettingChangeListenerMap.remove(key);
			}
		}
	}

	/**
	 * 获取当前版本号
	 */
	public String getVersionName() {
		PackageManager packageManager = getPackageManager();
		PackageInfo packInfo;
		String version = "";
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 获取当前版本号
	 */
	public int getVersionCode() {
		PackageManager packageManager = getPackageManager();
		PackageInfo packInfo;
		int versionCode = 0;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			versionCode = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	private void checkUIThread() {
		if (!isRunOnUIThread())
			throw new RuntimeException("方法只能在主线程调用！");
	}

	/**
	 * 判断当前线程是否是主线程
	 * 
	 * @return
	 */
	private boolean isRunOnUIThread() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SettingKey settingKey = SettingKey.valueOf(SettingKey.class, key);
		List<OnSettingChangeListener> onSettingChangeListeners = onSettingChangeListenerMap.get(settingKey);
		if (onSettingChangeListeners != null) {
			for (OnSettingChangeListener onSettingChangeListener : onSettingChangeListeners) {
				onSettingChangeListener.onSettingChange(settingKey);
			}
		}
	}

    public boolean isAlreadyLeftFromForeground() {
        return alreadyLeftFromForeground;
    }

    public void setAlreadyLeftFromForeground(boolean alreadyLeftFromForeground) {
        this.alreadyLeftFromForeground = alreadyLeftFromForeground;
    }

    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(boolean passwordChanged) {
        this.passwordChanged = passwordChanged;
    }


}
