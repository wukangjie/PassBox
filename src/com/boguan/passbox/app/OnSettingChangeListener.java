package com.boguan.passbox.app;

import com.boguan.passbox.model.SettingKey;

/**
 * 用户设置变化监听器
 */
public interface OnSettingChangeListener {
	void onSettingChange(SettingKey key);
}
