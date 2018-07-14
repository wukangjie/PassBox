package com.boguan.passbox.service.LocalService.task;

import java.util.List;

import com.boguan.passbox.database.PasswordDatabase;
import com.boguan.passbox.model.AsyncResult;
import com.boguan.passbox.model.AsyncSingleTask;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordCallback;

/**
 * 异步获取所有密码任务
 */
public class GetAllPasswordTask extends AsyncSingleTask<List<Password>> {
	private PasswordDatabase passwordDatabase;
	private OnGetAllPasswordCallback onGetAllPasswordCallback;
	private String groupName;

	public GetAllPasswordTask(PasswordDatabase passwordDatabase, OnGetAllPasswordCallback onGetAllPasswordCallback,
			String groupName) {
		this.passwordDatabase = passwordDatabase;
		this.onGetAllPasswordCallback = onGetAllPasswordCallback;
		this.groupName = groupName;
	}

	@Override
	protected AsyncResult<List<Password>> doInBackground(AsyncResult<List<Password>> asyncResult) {
		List<Password> passwords;
		if (groupName == null)
			passwords = passwordDatabase.getAllPassword();
		else
			passwords = passwordDatabase.getAllPasswordByGroupName(groupName);
		asyncResult.setData(passwords);
		return asyncResult;
	}

	@Override
	protected void runOnUIThread(AsyncResult<List<Password>> asyncResult) {
		onGetAllPasswordCallback.onGetAllPassword(groupName, asyncResult.getData());
	}

}
