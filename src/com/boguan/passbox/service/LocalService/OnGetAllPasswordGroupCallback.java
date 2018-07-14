package com.boguan.passbox.service.LocalService;

import java.util.List;

import com.boguan.passbox.model.PasswordGroup;

public interface OnGetAllPasswordGroupCallback {
	public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups);
}
