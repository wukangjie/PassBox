package com.boguan.passbox.service.LocalService;

import com.boguan.passbox.model.PasswordGroup;

/**
 * 密码变化监听器
 * 
 * @author zengdexing
 * 
 */
public interface OnPasswordGroupChangeListener {

	/**
	 * 用户增加了新的密码
	 */
	public void onNewPasswordGroup(PasswordGroup passwordGroup);

	/**
	 * 组密码被删除了
	 */
	public void onDeletePasswordGroup(String passwordGroupName);

	
	public void onUpdateGroupName(String oldGroupName, String newGroupName);
}
