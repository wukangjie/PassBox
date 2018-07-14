package com.boguan.passbox.service.LocalService;

import java.util.List;

import com.boguan.passbox.model.Password;

public interface OnGetAllPasswordCallback {
	public void onGetAllPassword(String froupName, List<Password> passwords);
}
