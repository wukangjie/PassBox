package com.boguan.passbox.service.LocalService;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Binder;
import com.boguan.passbox.app.MyApplication;
import com.boguan.passbox.app.OnSettingChangeListener;
import com.boguan.passbox.database.PasswordDatabase;
import com.boguan.passbox.model.AsyncResult;
import com.boguan.passbox.model.AsyncSingleTask;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.PasswordGroup;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.service.LocalService.task.GetAllPasswordTask;

public class Mainbinder extends Binder {
	private MyApplication myApplication;
	private PasswordDatabase mPasswordDatabase;

	/** 密码变化监听器 */
	private List<OnPasswordChangeListener> mPasswordListeners = new ArrayList<OnPasswordChangeListener>();

	/** 密码分组变化监听 */
	private List<OnPasswordGroupChangeListener> mPasswordGroupListeners = new ArrayList<OnPasswordGroupChangeListener>();

	private OnSettingChangeListener mSettingChangeListener = new OnSettingChangeListener() {
		@Override
		public void onSettingChange(SettingKey key) {
			// 用户密码变化了，重新解密后再加密
			encodePasswd(myApplication.getSettingDecode(SettingKey.LOCK_PATTERN, "[]"));
		}
	};

	/** 重新解密 */
	private void encodePasswd(final String newPasswd) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				mPasswordDatabase.encodePasswd(newPasswd);
				return asyncResult;
			}
		}.execute();
	}

	public Mainbinder(Context context, MyApplication myApplication) {
		mPasswordDatabase = new PasswordDatabase(context);
		this.myApplication = myApplication;
		final String passwd = myApplication.getSettingDecode(SettingKey.LOCK_PATTERN, "[]");
		myApplication.registOnSettingChangeListener(SettingKey.LOCK_PATTERN, mSettingChangeListener);
		// 线程安全
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				mPasswordDatabase.setCurrentPasswd(passwd);
				mPasswordDatabase.getWritableDatabase();
				return asyncResult;
			}
		}.execute();
	}

	void onDestroy() {
		mPasswordDatabase.close();
		myApplication.unregistOnSettingChangeListener(SettingKey.LOCK_PATTERN, mSettingChangeListener);
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				mPasswordListeners.clear();
			}
		}.execute();
	}

	public void registOnPasswordGroupListener(final OnPasswordGroupChangeListener onPasswordGroupListener) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				mPasswordGroupListeners.add(onPasswordGroupListener);
			}
		}.execute();
	}

	public void unregistOnPasswordGroupListener(final OnPasswordGroupChangeListener onPasswordGroupListener) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				mPasswordGroupListeners.remove(onPasswordGroupListener);
			}
		}.execute();
	}

	public void registOnPasswordListener(final OnPasswordChangeListener onPasswordListener) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				mPasswordListeners.add(onPasswordListener);
			}
		}.execute();
	}

	public void unregistOnPasswordListener(final OnPasswordChangeListener onPasswordListener) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				mPasswordListeners.remove(onPasswordListener);
			}
		}.execute();
	}

	public void getAllPassword(OnGetAllPasswordCallback onGetAllPasswordCallback, String groupName) {
		GetAllPasswordTask getAllPasswordTask = new GetAllPasswordTask(mPasswordDatabase, onGetAllPasswordCallback,
				groupName);
		getAllPasswordTask.execute();
	}

	public void getAllPassword(OnGetAllPasswordCallback onGetAllPasswordCallback) {
		GetAllPasswordTask getAllPasswordTask = new GetAllPasswordTask(mPasswordDatabase, onGetAllPasswordCallback, null);
		getAllPasswordTask.execute();
	}

	/**
	 * 删除密码
	 *
	 * @param id
	 *            密码ID
	 */
	public void deletePassword(final int id) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				int result = mPasswordDatabase.deletePasssword(id);
				asyncResult.setResult(result);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				for (OnPasswordChangeListener onPasswordListener : mPasswordListeners) {
					onPasswordListener.onDeletePassword(id);
				}
			}
		}.execute();
	}

	/**
	 * 删除所有密码
	 *
	 *
	 *
	 */
	public void deleteAllPassword() {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				int result = mPasswordDatabase.deleteAllPasssword();
				asyncResult.setResult(result);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				for (OnPasswordChangeListener onPasswordListener : mPasswordListeners) {
					onPasswordListener.onDeleteAllPassword();
				}
			}
		}.execute();
	}

	public void getPassword(final int id, final OnGetPasswordCallback onGetPasswordCallback) {
		new AsyncSingleTask<Password>() {
			@Override
			protected AsyncResult<Password> doInBackground(AsyncResult<Password> asyncResult) {
				Password password = mPasswordDatabase.getPassword(id);
				asyncResult.setData(password);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Password> asyncResult) {
				onGetPasswordCallback.onGetPassword(asyncResult.getData());
			}
		}.execute();
	}

	public void updatePassword(final Password password) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				int result = mPasswordDatabase.updatePassword(password);
				asyncResult.setResult(result);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				for (OnPasswordChangeListener onPasswordListener : mPasswordListeners) {
					onPasswordListener.onUpdatePassword(password);
				}
			}
		}.execute();
	}

	public void insertPassword(final Password password) {
		new AsyncSingleTask<Password>() {
			@Override
			protected AsyncResult<Password> doInBackground(AsyncResult<Password> asyncResult) {
				String newGroupName = password.getGroupName();

				/** 是否是新的分组 */
				boolean isNew = true;
				List<PasswordGroup> passwordGroups = mPasswordDatabase.getAllPasswordGroup();
				for (int i = 0; i < passwordGroups.size(); i++) {
					PasswordGroup passwordGroup = passwordGroups.get(i);
					if (passwordGroup.getGroupName().equals(newGroupName)) {
						isNew = false;
						break;
					}
				}

				if (isNew) {
					// 不存在的分组，添加
					PasswordGroup passwordGroup = new PasswordGroup();
					passwordGroup.setGroupName(newGroupName);
					mPasswordDatabase.addPasswordGroup(passwordGroup);
				}
				asyncResult.getBundle().putBoolean("isNew", isNew);

				int result = (int) mPasswordDatabase.insertPassword(password);
				password.setId(result);
				asyncResult.setData(password);
				return asyncResult;
			}
			@Override
			protected void runOnUIThread(AsyncResult<Password> asyncResult) {
				if (asyncResult.getBundle().getBoolean("isNew")) {
					PasswordGroup passwordGroup = new PasswordGroup();
					passwordGroup.setGroupName(asyncResult.getData().getGroupName());

					for (OnPasswordGroupChangeListener onPasswordGroupListener : mPasswordGroupListeners) {
						onPasswordGroupListener.onNewPasswordGroup(passwordGroup);
					}
				}

				for (OnPasswordChangeListener onPasswordListener : mPasswordListeners) {
					onPasswordListener.onNewPassword(asyncResult.getData());
				}
			}
		}.execute();
	}

	public void insertPasswordGroup(final PasswordGroup passwordGroup) {
		new AsyncSingleTask<PasswordGroup>() {
			@Override
			protected AsyncResult<PasswordGroup> doInBackground(AsyncResult<PasswordGroup> asyncResult) {
				String newGroupName = passwordGroup.getGroupName();

				boolean isNew = true;
				List<PasswordGroup> passwordGroups = mPasswordDatabase.getAllPasswordGroup();
				for (int i = 0; i < passwordGroups.size(); i++) {
					PasswordGroup passwordGroup = passwordGroups.get(i);
					if (passwordGroup.getGroupName().equals(newGroupName)) {
						isNew = false;
						break;
					}
				}

				if (isNew) {
					PasswordGroup passwordGroup = new PasswordGroup();
					passwordGroup.setGroupName(newGroupName);
					mPasswordDatabase.addPasswordGroup(passwordGroup);
				}
				asyncResult.getBundle().putBoolean("isNew", isNew);
				asyncResult.setData(passwordGroup);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<PasswordGroup> asyncResult) {
				if (asyncResult.getBundle().getBoolean("isNew")) {
					for (OnPasswordGroupChangeListener onPasswordGroupListener : mPasswordGroupListeners) {
						onPasswordGroupListener.onNewPasswordGroup(asyncResult.getData());
					}
				}
			}
		}.execute();
	}
	public void insertPasswordGroup(final List<PasswordGroup> passwordGroup) {
		for (int i = 0; i < passwordGroup.size(); i++) {
			final int finalI = i;
			new AsyncSingleTask<PasswordGroup>() {
			@Override
			protected AsyncResult<PasswordGroup> doInBackground(AsyncResult<PasswordGroup> asyncResult) {
				String newGroupName = passwordGroup.get(finalI).getGroupName();

				boolean isNew = true;
				List<PasswordGroup> passwordGroups = mPasswordDatabase.getAllPasswordGroup();
				for (int i = 0; i < passwordGroups.size(); i++) {
					PasswordGroup passwordGroup = passwordGroups.get(i);
					if (passwordGroup.getGroupName().equals(newGroupName)) {
						isNew = false;
						break;
					}
				}

				if (isNew) {
					PasswordGroup passwordGroup = new PasswordGroup();
					passwordGroup.setGroupName(newGroupName);
					mPasswordDatabase.addPasswordGroup(passwordGroup);
				}
				asyncResult.getBundle().putBoolean("isNew", isNew);
				asyncResult.setData(passwordGroup.get(finalI));
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<PasswordGroup> asyncResult) {
				if (asyncResult.getBundle().getBoolean("isNew")) {
					for (OnPasswordGroupChangeListener onPasswordGroupListener : mPasswordGroupListeners) {
						onPasswordGroupListener.onNewPasswordGroup(asyncResult.getData());
					}
				}
			}
		}.execute();
	}
	}



	/**
	 * 删除密码分组，包括密码分住下的所有密码都会被删除
	 *
	 * @param passwordGroupName
	 *            分组名
	 */
	public void deletePasswordgroup(final String passwordGroupName) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				int count = mPasswordDatabase.deletePasswordGroup(passwordGroupName);
				asyncResult.setResult(count);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				if (asyncResult.getResult() > 0) {
					for (OnPasswordGroupChangeListener onPasswordGroupListener : mPasswordGroupListeners) {
						onPasswordGroupListener.onDeletePasswordGroup(passwordGroupName);
					}
				}
			}
		}.execute();
	}

	/**
	 * 更新组名字
	 *
	 * @param oldGroupName
	 * @param newGroupName
	 */
	public void updatePasswdGroupName(final String oldGroupName, final String newGroupName) {
		new AsyncSingleTask<Void>() {
			@Override
			protected AsyncResult<Void> doInBackground(AsyncResult<Void> asyncResult) {
				mPasswordDatabase.updatePasswdGroupName(oldGroupName, newGroupName);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<Void> asyncResult) {
				for (OnPasswordGroupChangeListener onPasswordGroupListener : mPasswordGroupListeners) {
					onPasswordGroupListener.onUpdateGroupName(oldGroupName, newGroupName);
				}
			}
		}.execute();
	}

	public void getAllPasswordGroup(final OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback) {
		new AsyncSingleTask<List<PasswordGroup>>() {
			@Override
			protected AsyncResult<List<PasswordGroup>> doInBackground(AsyncResult<List<PasswordGroup>> asyncResult) {
				List<PasswordGroup> list = mPasswordDatabase.getAllPasswordGroup();
				asyncResult.setData(list);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<List<PasswordGroup>> asyncResult) {
				onGetAllPasswordGroupCallback.onGetAllPasswordGroup(asyncResult.getData());
			}
		}.execute();
	}
}
